package io.logbee.keyscore.frontier.auth

import java.io.{BufferedInputStream, File, InputStream}
import java.math.BigInteger
import java.security.cert.{Certificate, CertificateFactory}
import java.security.spec.RSAPublicKeySpec
import java.security.{KeyFactory, KeyStore, PublicKey, SecureRandom}
import java.util.Base64

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.server.Directives.{extractCredentials, onComplete, provide, reject}
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Directive1}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}
import org.json4s.DefaultFormats
import org.json4s.native.Serialization
import org.keycloak.TokenVerifier
import org.keycloak.adapters.{KeycloakDeployment, KeycloakDeploymentBuilder}
import org.keycloak.jose.jws.AlgorithmType
import org.keycloak.representations.AccessToken

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

trait AuthorizationHandler extends Json4sSupport {
  implicit def executionContext: ExecutionContext

  implicit def materializer: ActorMaterializer

  implicit def system: ActorSystem

  def log: LoggingAdapter

  val config = ConfigFactory.load()

  def authorize: Directive1[AccessToken] = if (system.settings.config.getBoolean("authentication.enable-keycloak"))
    extractCredentials.flatMap {
      case Some(OAuth2BearerToken(token)) =>
        onComplete(verifyToken(token)).flatMap {
          case Success(Some(t)) =>
            provide(t)
          case e =>
            log.warning(s"token $token is not valid")
            log.debug(s"Failure: $e")
            reject(AuthorizationFailedRejection)
        }
      case _ =>
        log.warning("no token present in request")
        reject(AuthorizationFailedRejection)
    } else provide(null)

  private[auth] def verifyToken(token: String): Future[Option[AccessToken]] = {
    val tokenVerifier = TokenVerifier.create(token, classOf[AccessToken])
    for {
      publicKey <- publicKeys.map(_.get(tokenVerifier.getHeader.getKeyId))
    } yield publicKey match {
      case Some(publicKeyResult) =>
        val token = tokenVerifier.publicKey(publicKeyResult).verify().getToken
        if (!token.isExpired)
          Some(token)
        else {
          log.warning(s"Token is expired!")
          None
        }
      case None =>
        log.warning(s"no public key found for id ${tokenVerifier.getHeader.getKeyId}")
        None
    }
  }

  val keycloakDeployment: KeycloakDeployment = KeycloakDeploymentBuilder.build(getClass.getResourceAsStream("/keycloak.json"))

  def publicKeys: Future[Map[String, PublicKey]] = {
    implicit val serialization = Serialization
    implicit val formats = DefaultFormats
    val https = buildHttpsConnectionContext

    Http().singleRequest(HttpRequest(uri = keycloakDeployment.getJwksUrl),if (https != null) https else Http().createDefaultClientHttpsContext()).flatMap(response => {
      Unmarshal(response.entity).to[Keys].map(_.keys.map(k => (k.kid, generateKey(k))).toMap)
    })
  }

  private def buildHttpsConnectionContext: HttpsConnectionContext = {
    val password = "password".toCharArray // TODO: Get Password from somewhere safe ;)

    val ks: KeyStore = KeyStore.getInstance("PKCS12")
    ks.load(null, password)

    val cas = getTrustedCerts

    if (cas.isEmpty) null
    else {
      cas.zipWithIndex.foreach { case (ca, i) =>
        ks.setCertificateEntry("certificate" ++ i.toString, ca)
      }

      val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("SunX509")
      keyManagerFactory.init(ks, password)

      val sslConfig = AkkaSSLConfig()
      val config = sslConfig.config

      val trustManager: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
      trustManager.init(ks)

      val sslContext: SSLContext = SSLContext.getInstance("TLS")
      sslContext.init(keyManagerFactory.getKeyManagers, trustManager.getTrustManagers, new SecureRandom)

      ConnectionContext.https(sslContext)
    }
  }

  private def getTrustedCerts: List[Certificate] = {
    val cas = new ListBuffer[Certificate]()
    val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
    val trustedDir = getClass.getResource("/trusted-certs")

    if(trustedDir == null) return cas.toList

    val path = trustedDir.getPath
    val dir = new File(path)
    val fileIt = walkTree(dir).toIterator

    if(!fileIt.hasNext) return cas.toList

    fileIt.next()
    while (fileIt.hasNext) {
      val f = fileIt.next()
      val is: InputStream = getClass.getResourceAsStream("/trusted-certs/" ++ f.getName)
      val caInput: InputStream = new BufferedInputStream(is)
      val ca: Certificate = cf.generateCertificate(caInput)
      cas += ca
    }
    cas.toList
  }

  private def generateKey(keyData: KeyData): PublicKey = {
    val keyFactory = KeyFactory.getInstance(AlgorithmType.RSA.toString)
    val urlDecoder = Base64.getUrlDecoder
    val modulus = new BigInteger(1, urlDecoder.decode(keyData.n))
    val publicExponent = new BigInteger(1, urlDecoder.decode(keyData.e))
    keyFactory.generatePublic(new RSAPublicKeySpec(modulus, publicExponent))
  }

  private def walkTree(file: File): Iterable[File] = {
    val children = new Iterable[File] {
      def iterator = if (file.isDirectory) file.listFiles.iterator else Iterator.empty
    }
    Seq(file) ++: children.flatMap(walkTree(_))
  }

}

case class Keys(keys: Seq[KeyData])

case class KeyData(kid: String, n: String, e: String)
