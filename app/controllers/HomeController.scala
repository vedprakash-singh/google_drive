package controllers

import java.io.{ByteArrayOutputStream, InputStreamReader, BufferedReader, DataOutputStream}
import java.net.URL
import java.util
import javax.inject._
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import sun.net.www.protocol.https.HttpsURLConnectionImpl
import play.api.mvc._
import com.google.api.client.googleapis.auth.oauth2.{GoogleCredential, GoogleBrowserClientRequestUrl}
import scala.collection.JavaConversions._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {

  val redirectURI = "http://localhost:9000/callback"
  val CLIENT_ID = "196880177283-1377e3ib4kd142mfvd1e5q0vqgfjhru0.apps.googleusercontent.com"
  val CLIENT_SECRET = "7yipeBkkWRJhaI0vS9NJZOtl"
  val httpTransport = new NetHttpTransport
  val jsonFactory = new JacksonFactory

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def authenticate = Action { implicit request =>
    val urlToRedirect = new GoogleBrowserClientRequestUrl(CLIENT_ID,
      redirectURI, util.Arrays.asList(
        "https://www.googleapis.com/auth/plus.login",
        "https://www.googleapis.com/auth/drive")).set("access_type", "offline")
      .set("response_type", "code").build()
    Redirect(urlToRedirect)
  }

  def callback = Action { implicit request =>
    val code = request.queryString("code").toList(0)
    val url = "https://accounts.google.com/o/oauth2/token"
    val obj = new URL(url)
    val con = obj.openConnection().asInstanceOf[HttpsURLConnectionImpl]

    con.setRequestMethod("POST")
    con.setRequestProperty("User-Agent", USER_AGENT)
    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5")

    val urlParameters = s"code=${code}&client_id=${CLIENT_ID}&client_secret=${CLIENT_SECRET}" +
      s"&redirect_uri=${redirectURI}&grant_type=authorization_code&Content-Type=application/x-www-form-urlencoded"
    con.setDoOutput(true)
    val wr = new DataOutputStream(con.getOutputStream)
    wr.writeBytes(urlParameters)
    wr.flush
    wr.close

    val inputStream = if (con.getResponseCode() == 200) {
      con.getInputStream()
    } else {
      con.getErrorStream()
    }
    val in = new BufferedReader(new InputStreamReader(inputStream))
    val response = new StringBuffer
    while (in.readLine != null) {
      response.append(in.readLine)
    }
    val accessToken = response.toString.split(",").toList(0).split(":").toList(1)
    val files = getAllDocumentsFromGoogleDocs(accessToken)
    val fileId = "0B43IZoz6cHYdVXc0YUtOZndGRG8"
    println(s"Files:::${files}")
    getFile(fileId, accessToken)
    in.close
    Ok(response.toString)
  }

  def getFile(fileId: String, accessToken: String) = {
    val driver = prepareGoogleDrive(accessToken)
    val outputStream = new ByteArrayOutputStream()
    driver.files().get(fileId).executeMediaAndDownloadTo(outputStream)

  }

  /**
    * Set Up Google App Credentials
    */
  def prepareGoogleDrive(accessToken: String): Drive = {
    //Build the Google credentials and make the Drive ready to interact
    val credential = new GoogleCredential.Builder()
      .setJsonFactory(jsonFactory)
      .setTransport(httpTransport)
      .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
      .build()
    credential.setAccessToken(accessToken)
    //Create a new authorized API client
    new Drive.Builder(httpTransport, jsonFactory, credential).build()
  }

  /**
    * Get All Files From Google Drive
    */

  def getAllDocumentsFromGoogleDocs(code: String): List[(String, String)] = {
    val service = prepareGoogleDrive(code)
    var result: List[File] = Nil
    val request = service.files.list
    do {
      val files = request.execute
      result ++= (files.getFiles)
      request.setPageToken(files.getNextPageToken)
    } while (request.getPageToken() != null && request.getPageToken().length() > 0)

    result map {
      case a => (a.getName, a.getId)
    }
  }

  /*def authDropbox(String dropBoxAppKey, String dropBoxAppSecret)
  throws IOException, DbxException {
    DbxAppInfo dbxAppInfo = new DbxAppInfo(dropBoxAppKey, dropBoxAppSecret);
    DbxRequestConfig dbxRequestConfig = new DbxRequestConfig(
      "JavaDropboxTutorial/1.0", Locale.getDefault().toString());
    DbxWebAuthNoRedirect dbxWebAuthNoRedirect = new DbxWebAuthNoRedirect(
      dbxRequestConfig, dbxAppInfo);
    String authorizeUrl = dbxWebAuthNoRedirect.start();
    System.out.println("1. Authorize: Go to URL and click Allow : "
      + authorizeUrl);
    System.out
      .println("2. Auth Code: Copy authorization code and input here ");
    String dropboxAuthCode = new BufferedReader(new InputStreamReader(
      System.in)).readLine().trim();
    DbxAuthFinish authFinish = dbxWebAuthNoRedirect.finish(dropboxAuthCode);
    String authAccessToken = authFinish.accessToken;
    dbxClient = new DbxClient(dbxRequestConfig, authAccessToken);
    System.out.println("Dropbox Account Name: "
      + dbxClient.getAccountInfo().displayName);

    return dbxClient;
  }*/

}
