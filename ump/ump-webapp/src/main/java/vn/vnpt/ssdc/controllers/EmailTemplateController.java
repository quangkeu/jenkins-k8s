package vn.vnpt.ssdc.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.vnpt.ssdc.api.client.EmailTemplateClient;
import vn.vnpt.ssdc.api.model.EmailTemplat;
import vn.vnpt.ssdc.models.EmailPaginator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lamborgini on 6/7/2017.
 */
@Controller
public class EmailTemplateController extends BaseController {

    private static final String EMAIL_PAGE = "email/email";

    @Autowired
    private EmailTemplateClient emailTemplateClient;


    @GetMapping("/email")
    public String index(Model model, @RequestParam HashMap<String, String> requestParams) {
        EmailTemplat[] emailTemplatList = emailTemplateClient.findAll();
        loadData(model, requestParams);
        return EMAIL_PAGE;
    }

    @GetMapping("/email/search")
    public String search(Model model, @RequestParam Map<String, String> requestParams) {
        loadData(model, requestParams);
        return EMAIL_PAGE;
    }

    @PostMapping("/email-add-new")
    @ResponseBody
    public boolean addnewEmail(Model model, @RequestParam HashMap<String, String> requestParams) {
        boolean result = false;
        try {
            if (requestParams != null && requestParams.size() > 0) {
                if(requestParams.get("mode").equals("add")){
                    EmailTemplat emailTemplat = new EmailTemplat();
                    emailTemplat.id = requestParams.get("id");
                    emailTemplat.description = requestParams.get("description");
                    emailTemplat.value = requestParams.get("value");
                    emailTemplateClient.create(emailTemplat);
                } else {
                    EmailTemplat emailTemplat = emailTemplateClient.get(requestParams.get("id"));
                    emailTemplat.description = requestParams.get("description");
                    emailTemplat.value = requestParams.get("value");
                    emailTemplateClient.update(requestParams.get("id"),emailTemplat);
                }

                result = true;
            }
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    @PostMapping("/delete-email")
    @ResponseBody
    public boolean deleteEmail(@RequestParam Map<String, String> params) {
        boolean result = false;
        if (params.keySet().contains("deleteIds")) {
            String[] paramRole = params.get("deleteIds").substring(0, params.get("deleteIds").length() - 1).split(",");
            if (paramRole.length > 0) {
                try {
                    for (int i = 0; i < paramRole.length; i++) {
                        emailTemplateClient.delete(paramRole[i]);
                        result = true;
                    }
                } catch (Exception e) {
                    result = false;
                }
            }
        }
        return result;
    }

    private void loadData(Model model, Map<String, String> requestParams) {
        EmailPaginator emailPaginator = new EmailPaginator();
        emailPaginator.emailTemplateClient = emailTemplateClient;
        emailPaginator.parseParam(requestParams);
        emailPaginator.loadResult(requestParams);

        model.addAttribute("paginator", emailPaginator);
        model.addAttribute("itemPerPage", requestParams.size() == 0 ? "20" : requestParams.get("limit"));

    }
}
