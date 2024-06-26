package com.kmaebashi.blog.service;

import com.kmaebashi.blog.dbaccess.UserDbAccess;
import com.kmaebashi.blog.dto.UserDto;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.RedirectResult;
import com.kmaebashi.nctfw.ServiceInvoker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.nio.file.Path;

public class LoginService {
    private LoginService() {}

    public static DocumentResult showPage(ServiceInvoker invoker) {
        return invoker.invoke((context) -> {
            Path htmlPath = context.getHtmlTemplateDirectory().resolve("login.html");
            Document doc = LoginService.render(htmlPath, false);
            return new DocumentResult(doc);
        });
    }

    public static boolean checkPassword(ServiceInvoker invoker, String userId, String password) {
        return invoker.invoke((context) -> {
            UserDto userDto = UserDbAccess.getUser(context.getDbAccessInvoker(), userId.trim());
            if (userDto == null || !Util.checkPassword(password.trim(), userDto.password)) {
                return false;
            } else {
                return true;
            }
        });
    }

    public static DocumentResult doLogin(ServiceInvoker invoker, String userId, String password) {
        return invoker.invoke((context) -> {
            UserDto userDto = UserDbAccess.getUser(context.getDbAccessInvoker(), userId.trim());
            if (userDto == null || !Util.checkPassword(password.trim(), userDto.password)) {
                throw new BadRequestException("ログインエラー");
            }
            return null;
        });
    }

    private static Document render(Path htmlPath, boolean isError)
            throws Exception {
        Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");

        return doc;
    }

}
