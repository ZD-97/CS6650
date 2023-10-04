package cs6650.hw1.dazhu;

import java.io.InputStream;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import com.google.gson.Gson;

@WebServlet(name = "AlbumStoreServlet",value = "/albums/*")
public class AlbumStoreServlet extends HttpServlet {
  private Gson gson = new Gson();
  class imageMetaData {
    private String albumID;
    private String imageSize;
    imageMetaData(String albumID, String imageSize) {
      this.albumID = albumID;
      this.imageSize = imageSize;
    }
  }

  class albumInfo {
    private String artist;
    private String title;
    private String year;
    albumInfo(String artist, String title, String year) {
      this.artist = artist;
      this.title = title;
      this.year = year;
    }

  }

  class errorMsg {
    private String errorMsg;

    public errorMsg (String msg) {
        errorMsg = msg;
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");

    if (false) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      InputStream inputStream = req.getInputStream();
      int size = 0;
      byte[] buffer = new byte[8192]; // Use an appropriate buffer size
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        size += bytesRead;
      }

      res.setStatus(HttpServletResponse.SC_OK);
      imageMetaData imageData = new imageMetaData("123",String.valueOf(size));
      String str = this.gson.toJson(imageData);
      PrintWriter out = res.getWriter();
      out.print(str);
      out.flush();
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing paramterers");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isGetUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      errorMsg getErrorMsgGet = new errorMsg("You need to specify album id");
      String errorString = this.gson.toJson(getErrorMsgGet);
      PrintWriter out = res.getWriter();
      out.print(errorString);
      out.flush();
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
      albumInfo album1 = new albumInfo("Sex Pistols", "Never Mind The Bollocks!", "1977");
      String albumString = this.gson.toJson(album1);
      PrintWriter out = res.getWriter();
      out.print(albumString);
      out.flush();
    }
  }

  private boolean isGetUrlValid(String[] urlPath) {
    // TODO: validate the request url path according to the API spec
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
    if(urlPath.length == 2 && urlPath[1].length() > 0) return true;
    return false;
  }

}
