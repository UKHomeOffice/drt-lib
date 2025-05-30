package uk.gov.homeoffice.drt.keycloak

import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.model.headers.{Accept, Authorization, OAuth2BearerToken}
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.apache.pekko.stream.Materializer
import org.apache.pekko.util.Timeout
import org.slf4j.{Logger, LoggerFactory}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

case class KeyCloakClient(token: String, keyCloakUrl: String, sendHttpRequest: HttpRequest => Future[HttpResponse])
                         (implicit val ec: ExecutionContext, mat: Materializer)
  extends KeyCloakUserParserProtocol {

  def log: Logger = LoggerFactory.getLogger(getClass)

  implicit val timeout: Timeout = Timeout(1 minute)

  def logResponse(requestName: String, resp: HttpResponse): HttpResponse = {
    if (resp.status.isFailure)
      log.error(s"Error when calling $requestName on KeyCloak API Status code: ${resp.status} Response:<${resp.entity.toString}>")

    resp
  }

  def pipeline(method: HttpMethod, uri: String, requestName: String): Future[HttpResponse] = {
    val request = HttpRequest(method, Uri(uri))
    val requestWithHeaders = request
      .addHeader(Accept(MediaTypes.`application/json`))
      .addHeader(Authorization(OAuth2BearerToken(token)))
    sendHttpRequest(requestWithHeaders).map { r =>
      logResponse(requestName, r)
      r
    }
  }

  def getUserForEmail(email: String): Future[Option[KeyCloakUser]] = {
    val uri = keyCloakUrl + s"/users?email=$email"
    log.info(s"Calling key cloak: $uri")
    pipeline(HttpMethods.GET, uri, "getUsersForEmail")
      .flatMap { r => Unmarshal(r).to[List[KeyCloakUser]] }.map(_.headOption)
  }

  def getUsers(max: Int = 100, offset: Int = 0): Future[List[KeyCloakUser]] = {
    val uri = keyCloakUrl + s"/users?max=$max&first=$offset"
    log.info(s"Calling key cloak: $uri")
    pipeline(HttpMethods.GET, uri, "getUsers").flatMap { r => Unmarshal(r).to[List[KeyCloakUser]] }
  }

  def getUserByUsername(username: String): Future[Option[KeyCloakUser]] = {
    val uri = keyCloakUrl + s"/users?username=$username"
    log.info(s"Calling key cloak: $uri")
    pipeline(HttpMethods.GET, uri, "getUsersForUsername")
      .flatMap { r => Unmarshal(r).to[List[KeyCloakUser]] }.map(_.headOption)
  }

  def getAllUsers(offset: Int = 0): Seq[KeyCloakUser] = {
    val users = Await.result(getUsers(50, offset), 2 seconds)
    if (users.isEmpty) Nil else users ++ getAllUsers(offset + 50)
  }

  def removeUser(userId: String): Future[HttpResponse] = {
    log.info(s"Removing $userId")
    val uri = s"$keyCloakUrl/users/$userId"
    pipeline(HttpMethods.DELETE, uri, "removeUserFromGroup")
  }

  def logUserOut(userId: String): Future[HttpResponse] = {
    log.info(s"Logout $userId")
    val uri = s"$keyCloakUrl/users/$userId/logout"
    pipeline(HttpMethods.POST, uri, "logoutUser")
  }

  def getUserGroups(userId: String): Future[List[KeyCloakGroup]] = {
    val uri = keyCloakUrl + s"/users/$userId/groups"
    log.info(s"Calling key cloak: $uri")
    pipeline(HttpMethods.GET, uri, "getUserGroups").flatMap { r => Unmarshal(r).to[List[KeyCloakGroup]] }
  }

  def getGroups: Future[List[KeyCloakGroup]] = {
    val uri = keyCloakUrl + "/groups"
    log.info(s"Calling key cloak: $uri")
    pipeline(HttpMethods.GET, uri, "getGroups").flatMap { r => Unmarshal(r).to[List[KeyCloakGroup]] }
  }

  def getUsersInGroup(groupName: String, max: Int = 1000): Future[List[KeyCloakUser]] = {
    val futureMaybeId: Future[Option[String]] = getGroups.map(gs => gs.find(_.name == groupName).map(_.id))

    futureMaybeId.flatMap {
      case Some(id) =>
        val uri = keyCloakUrl + s"/groups/$id/members?max=$max"
        pipeline(HttpMethods.GET, uri, "getUsersInGroup").flatMap { r => Unmarshal(r).to[List[KeyCloakUser]] }
      case None => Future(List())
    }
  }

  def getUsersNotInGroup(groupName: String): Future[List[KeyCloakUser]] = {

    val futureUsersInGroup: Future[List[KeyCloakUser]] = getUsersInGroup(groupName)
    val futureAllUsers: Future[List[KeyCloakUser]] = getUsers()

    for {
      usersInGroup <- futureUsersInGroup
      allUsers <- futureAllUsers
    } yield allUsers.filterNot(usersInGroup.toSet)
  }

  def addUserToGroup(userId: String, groupId: String): Future[HttpResponse] = {
    log.info(s"Adding $userId to $groupId")
    val uri = s"$keyCloakUrl/users/$userId/groups/$groupId"
    pipeline(HttpMethods.PUT, uri, "addUserToGroup")
  }

  def removeUserFromGroup(userId: String, groupId: String): Future[HttpResponse] = {
    log.info(s"Removing $userId from $groupId")
    val uri = s"$keyCloakUrl/users/$userId/groups/$groupId"
    pipeline(HttpMethods.DELETE, uri, "removeUserFromGroup")
  }
}

trait KeyCloakUserParserProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val keyCloakUserFormatParser: RootJsonFormat[KeyCloakUser] = jsonFormat7(KeyCloakUser)

  implicit val keyCloakGroupFormat: RootJsonFormat[KeyCloakGroup] = jsonFormat3(KeyCloakGroup)
}
