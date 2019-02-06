package io.logbee.keyscore.frontier.auth

import java.math.BigInteger
import java.security.spec.RSAPublicKeySpec
import java.security.{KeyFactory, PublicKey}
import java.util.Base64

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.model.{HttpEntity, HttpRequest}
import akka.http.scaladsl.server.Directives.{extractCredentials, onComplete, provide, reject}
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Directive1}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.DefaultFormats
import org.json4s.native.Serialization
import org.keycloak.TokenVerifier
import org.keycloak.adapters.{KeycloakDeployment, KeycloakDeploymentBuilder}
import org.keycloak.jose.jws.AlgorithmType
import org.keycloak.representations.AccessToken

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

trait AuthorizationHandler extends Json4sSupport {
  implicit def executionContext: ExecutionContext

  implicit def materializer: ActorMaterializer

  implicit def system: ActorSystem

  def log: LoggingAdapter

  def authorize: Directive1[AccessToken] =
    extractCredentials.flatMap {
      case Some(OAuth2BearerToken(token)) =>
        onComplete(verifyToken(token)).flatMap {
          case Success(Some(t)) =>
            provide(t)
          case _ =>
            log.warning(s"token $token is not valid")
            reject(AuthorizationFailedRejection)
        }
      case _ =>
        log.warning("no token present in request")
        reject(AuthorizationFailedRejection)
    }

  private def verifyToken(token: String): Future[Option[AccessToken]] = {
    val tokenVerifier = TokenVerifier.create(token, classOf[AccessToken])
      .withDefaultChecks()
    for {
      publicKey <- publicKeys.map(_.get(tokenVerifier.getHeader.getKeyId))
    } yield publicKey match {
      case Some(publicKey) =>
        val token = tokenVerifier.publicKey(publicKey).verify().getToken
        Some(token)
      case None =>
        log.warning(s"no public key found for id ${tokenVerifier.getHeader.getKeyId}")
        None
    }
  }

  val keycloakDeployment: KeycloakDeployment = KeycloakDeploymentBuilder.build(getClass.getResourceAsStream("/keycloak.json"))


  case class Keys(keys: Seq[KeyData])

  case class KeyData(kid: String, n: String, e: String)

  def publicKeys: Future[Map[String, PublicKey]] = {
    implicit val serialization = Serialization
    implicit val formats = DefaultFormats

    /*val badSslConfig = AkkaSSLConfig().mapSettings(s => s.withLoose(s.loose.withAcceptAnyCertificate(true)))
    val badCtx = Http().createClientHttpsContext(badSslConfig)*/

    Http().singleRequest(HttpRequest(uri = keycloakDeployment.getJwksUrl)).flatMap(response => {
      Unmarshal(response.entity.asInstanceOf[HttpEntity]).to[Keys].map(_.keys.map(k => (k.kid, generateKey(k))).toMap)
    })
  }

  private def generateKey(keyData: KeyData): PublicKey = {
    val keyFactory = KeyFactory.getInstance(AlgorithmType.RSA.toString)
    val urlDecoder = Base64.getUrlDecoder
    val modulus = new BigInteger(1, urlDecoder.decode(keyData.n))
    val publicExponent = new BigInteger(1, urlDecoder.decode(keyData.e))
    keyFactory.generatePublic(new RSAPublicKeySpec(modulus, publicExponent))
  }

}
