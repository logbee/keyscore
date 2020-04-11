package io.logbee.keyscore.pipeline.contrib.elasticsearch

import java.security.cert.X509Certificate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.stream.SinkShape
import com.google.protobuf.util.{Durations, Timestamps}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Value.Empty
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.Hashing._
import io.logbee.keyscore.pipeline.api.{LogicParameters, SinkLogic}
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION
import javax.net.ssl.{HostnameVerifier, SSLContext, SSLSession, X509TrustManager}
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback
import org.elasticsearch.client.{RequestOptions, RestClient, RestHighLevelClient}

import scala.concurrent.duration._
import scala.language.postfixOps

object ElasticsearchSinkLogic extends Described {
  import io.logbee.keyscore.model.util.ToOption.T2OptionT

  val urlParameter = TextParameterDescriptor(
    ref = "elastic.url",
    ParameterInfo(
      displayName = TextRef("elastic.url.displayName"),
      description = TextRef("elastic.url.description"),
    ),
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
    ),
    defaultValue = "https://example.com:9200",
    mandatory = true
  )
  
  val indexParameter = TextParameterDescriptor(
    ref = "elastic.index",
    ParameterInfo(
      displayName = TextRef("elastic.index.displayName"),
      description = TextRef("elastic.index.description")
    ),
    defaultValue = "doc",
    mandatory = true
  )
  
  val indexPostfixParameter = ChoiceParameterDescriptor(
    ref = "elastic.indexPostfix",
    ParameterInfo(
      displayName = TextRef("elastic.indexPostfix.displayName"),
      description = TextRef("elastic.indexPostfix.description"),
    ),
    min = 1,
    max = 1,
    choices = Seq(
      Choice(
        name = IndexPostfix.None.toString,
        displayName = TextRef("elastic.indexPostfix.none.displayName"),
        description = TextRef("elastic.indexPostfix.none.description"),
      ),
      Choice(
        name = IndexPostfix.Day.toString,
        displayName = TextRef("elastic.indexPostfix.day.displayName"),
        description = TextRef("elastic.indexPostfix.day.description"),
      ),
      Choice(
        name = IndexPostfix.Month.toString,
        displayName = TextRef("elastic.indexPostfix.month.displayName"),
        description = TextRef("elastic.indexPostfix.month.description"),
      ),
    ),
  )
  
  val authenticationRequiredParameter = BooleanParameterDescriptor(
    ref = "elastic.authentication.required",
    ParameterInfo(
      displayName = TextRef("elastic.authentication.required.displayName"),
      description = TextRef("elastic.authentication.required.description")
    ),
    defaultValue = false,
  )

  val authenticationUserParameter = TextParameterDescriptor(
    ref = "elastic.authentication.user",
    ParameterInfo(
      displayName = TextRef("elastic.authentication.username.displayName"),
      description = TextRef("elastic.authentication.username.description")
    ),
  )

  val authenticationPasswordParameter = PasswordParameterDescriptor(
    ref = "elastic.authentication.password",
    ParameterInfo(
      displayName = TextRef("elastic.authentication.password.displayName"),
      description = TextRef("elastic.authentication.password.description")
    ),
    maxLength = Int.MaxValue
  )
  
  val disableCertVerificationParameter = BooleanParameterDescriptor(
    ref = "elastic.disableCertVerification",
    ParameterInfo(
      displayName = TextRef("elastic.disableCertVerification.displayName"),
      description = TextRef("elastic.disableCertVerification.description"),
    ),
    defaultValue = false,
  )

  val documentIdFieldNameParameter = TextParameterDescriptor(
    ref = "elastic.documentId",
    ParameterInfo(
      displayName = TextRef("elastic.documentId.displayName"),
      description = TextRef("elastic.documentId.description")
    )
  )
  override def describe = Descriptor(
    ref = "6693c39e-6261-11e8-adc0-fa7ae01bbebc",
    describes = SinkDescriptor(
      name = classOf[ElasticsearchSinkLogic].getName,
      displayName = TextRef("elastic.displayName"),
      description = TextRef("elastic.description"),
      categories = Seq(CommonCategories.SINK, Category("Elasticsearch")),
      parameters = Seq(
        urlParameter,
        indexParameter,
        indexPostfixParameter,
        authenticationRequiredParameter,
        authenticationUserParameter,
        authenticationPasswordParameter,
        disableCertVerificationParameter,
        documentIdFieldNameParameter
      ),
      icon = Icon.fromClass(classOf[ElasticsearchSinkLogic]),
      maturity = Maturity.Development
    ),
    localization = Localization.fromResourceBundle(
      bundleName = classOf[ElasticsearchSinkLogic].getName,
      Locale.ENGLISH, Locale.GERMAN) ++ CATEGORY_LOCALIZATION
  )
}

class ElasticsearchSinkLogic(parameters: LogicParameters, shape: SinkShape[Dataset]) extends SinkLogic(parameters, shape) {

  private val maxRetries = 3
  private val retryInterval = 1 second

  private var elasticIndexBaseName: String = ElasticsearchSinkLogic.indexParameter.defaultValue
  private var indexPostfixChoice: IndexPostfix = IndexPostfix.None

  private var documentIdFieldName: String = ""

  private var elasticClient: RestHighLevelClient = _

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
    pull(in)
  }

  override def configure(configuration: Configuration): Unit = {
    import ElasticsearchSinkLogic._

    elasticIndexBaseName = configuration.getValueOrDefault(indexParameter, elasticIndexBaseName)
    indexPostfixChoice = IndexPostfix.fromString(configuration.getValueOrDefault(indexPostfixParameter, IndexPostfix.None.toString))
    documentIdFieldName = configuration.getValueOrDefault(documentIdFieldNameParameter, documentIdFieldName)

    val username = configuration.getValueOrDefault(authenticationUserParameter, authenticationUserParameter.defaultValue)
    val password = configuration.getValueOrDefault(authenticationPasswordParameter, authenticationPasswordParameter.defaultValue)

    if (configuration.getValueOrDefault(authenticationRequiredParameter, authenticationRequiredParameter.defaultValue)) {
      if (username.isEmpty | password.isEmpty) {
        val missingInfo = if (username.isEmpty && password.isEmpty) "credentials"
                          else if (username.isEmpty) "username"
                          else "password"
        val ex = new IllegalArgumentException(s"Authentication configured as 'required', but no $missingInfo provided.")
        log.error(ex, "Invalid configuration.")
        failStage(ex)
      }
    }

    val disableCertVerification = configuration.getValueOrDefault(disableCertVerificationParameter, disableCertVerificationParameter.defaultValue)
    
    val url = {
      var urlString = configuration.getValueOrDefault(urlParameter, urlParameter.defaultValue)
      while (urlString.endsWith("/")) {
        urlString = urlString.substring(0, urlString.length - 1)
      }
      urlString
    }

    elasticClient = new RestHighLevelClient(
      RestClient.builder(
        HttpHost.create(url) 
      ).setHttpClientConfigCallback(
        new HttpClientConfigCallback {
          override def customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder): HttpAsyncClientBuilder = {
            val basicCredentialsProvider = new BasicCredentialsProvider()
            basicCredentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password))
            httpClientBuilder.setDefaultCredentialsProvider(basicCredentialsProvider)

            if (disableCertVerification) {
              val sslContext = SSLContext.getInstance("TLS")
              sslContext.init(null, Array(new X509TrustManager {
                override def getAcceptedIssuers: Array[X509Certificate] = Array.empty[X509Certificate]
                override def checkClientTrusted(chain: Array[X509Certificate], authType: String): Unit = {}
                override def checkServerTrusted(chain: Array[X509Certificate], authType: String): Unit = {}
              }), new java.security.SecureRandom())
              httpClientBuilder.setSSLHostnameVerifier(new HostnameVerifier {
                override def verify(hostname: String, session: SSLSession): Boolean = true
              })
              httpClientBuilder.setSSLContext(sslContext)
            }
            
            httpClientBuilder
          }
        }
      )
    )
  }

  override def onPush(): Unit = {
    import scala.jdk.CollectionConverters._

    val bulk = new BulkRequest()

    val dataset = grab(in)

    dataset.records.map { record =>
      val nameValuePairs = record.fields.flatMap { case Field(name, value) =>
        val extractedValue = value match {
          case BooleanValue(value) => value
          case TextValue(value, _) => value
          case BinaryValue(value, _) => value
          case NumberValue(value) => value
          case DecimalValue(value) => value
          case timestampValue: TimestampValue => Timestamps.toString(timestampValue)
          case durationValue: DurationValue => Durations.toString(durationValue)
          case HealthValue(value) => value
          case Empty => None
        }

        Some((name, extractedValue))

      }

      var documentIdFieldValue = record.fields.hashCode.base64
      if (documentIdFieldName.nonEmpty) {
        record.fields.find(_.name == documentIdFieldName) match {
          case Some(Field(_, TextValue(value, _))) =>
            documentIdFieldValue = value
          case _ =>
            log.debug("new hashValue generated")
        }
      }

      bulk.add(
        new UpdateRequest(elasticIndex, documentIdFieldValue)
          .doc(nameValuePairs.toMap.asJava)
          .`type`("doc") //TODO remove this when support for Elasticsearch 6.x is dropped
          .docAsUpsert(true)
      )
    }

    executeRequest(elasticClient, request = bulk, retriesLeft = maxRetries)
  }

  case class Resend(request: BulkRequest, retriesLeft: Int)

  override def onTimer(timerKey: Any): Unit = {
    timerKey match {
      case Resend(_, 0) =>
        log.debug("No retries left for previous request, dropping it.")
        pull(in)
      case Resend(request, retriesLeft) =>
        log.debug("Retrying previous request.")
        executeRequest(elasticClient, request, retriesLeft)
    }
  }

  def executeRequest(elasticClient: RestHighLevelClient, request: BulkRequest, retriesLeft: Int): Unit = {
    try {
      val response = elasticClient.bulk(request, RequestOptions.DEFAULT)
      
      if (response.hasFailures) {
        log.error("Elasticsearch responded to update request with failures: " + response.buildFailureMessage)
        scheduleOnce(Resend(request, retriesLeft - 1), retryInterval)
      }
      else {
        pull(in)
      }
    }
    catch {
      case ex: Exception =>
        log.error(ex, "Failed to send update request to Elasticsearch.")
        supervisor.fail(ex)
    }
  }

  override def postStop(): Unit = {
    if (elasticClient != null) {
      elasticClient.close()
    }
    super.postStop()
  }

  private def elasticIndex: String = {
    elasticIndexBaseName + {
      import IndexPostfix._
      indexPostfixChoice match {
        case None  => ""
        case Day   => LocalDate.now.format(DateTimeFormatter.ofPattern("-yyyy-MM-dd"))
        case Month => LocalDate.now.format(DateTimeFormatter.ofPattern("-yyyy-MM"))
      }
    }
  }
}
