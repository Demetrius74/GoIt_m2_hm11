package org.august;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

@WebServlet("/time")
public class TimeServlet extends HttpServlet {

    private TemplateEngine templateEngine;

    @Override
    public void init() {
        templateEngine = new TemplateEngine();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("text/html; charset=utf-8");

        String timezone = getTimezone(req);
        ZoneId zoneId = ZoneId.of(timezone);
        ZonedDateTime now = ZonedDateTime.now(zoneId);

        // Use WebContext directly instead of IWebContext
        WebContext context = new WebContext(req, resp, req.getServletContext());
        context.setVariable("now", now);
        context.setVariable("timezone", zoneId.getId());

        String html = templateEngine.process("time.html", context);
        resp.getWriter().println(html);

        Cookie cookie = new Cookie("lastTimezone", timezone);
        cookie.setMaxAge(60 * 60 * 24 * 7); // 1 week
        resp.addCookie(cookie);
    }

    private String getTimezone(HttpServletRequest req) {
        String timezone = req.getParameter("timezone");
        if (timezone == null || TimeZone.getTimeZone(timezone).getID().equals("GMT")) {
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("lastTimezone".equals(cookie.getName())) {
                        timezone = cookie.getValue();
                        break;
                    }
                }
            }
            if (timezone == null) {
                timezone = "UTC";
            }
        }
        return timezone;
    }
}
