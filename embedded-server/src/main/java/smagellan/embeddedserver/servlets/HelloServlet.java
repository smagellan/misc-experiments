package smagellan.embeddedserver.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;

public class HelloServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(final HttpServletRequest req,
                         final HttpServletResponse res) throws IOException {
        res.getWriter()
                .append(String.format("current time is %s",
                        LocalDateTime.now()));
        req.getSession().setAttribute("myattr", "val");
    }
}
