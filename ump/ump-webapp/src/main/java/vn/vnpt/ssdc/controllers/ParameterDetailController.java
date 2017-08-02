package vn.vnpt.ssdc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.vnpt.ssdc.api.client.ParameterDetailClient;
import vn.vnpt.ssdc.api.model.ParameterDetail;
import vn.vnpt.ssdc.utils.ObjectUtils;
import vn.vnpt.ssdc.utils.StringUtils;

@Controller
public class ParameterDetailController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ParameterDetailController.class);

    private final String INDEX_URL = "/parameter-detail";

    @Autowired
    ParameterDetailClient parameterDetailClient;

    @GetMapping(INDEX_URL + "/get-check-new-instance")
    @ResponseBody
    public Boolean getCheckNewInstance(@RequestParam(value = "path", defaultValue = "") String path) {
        Boolean check = false;

        // Validate path
        if(path.contains("{i}")) {
            return false;
        }

        // Get str069 name
        StringBuilder tr069Name = new StringBuilder();
        String[] pathArr = path.split("[.]");
        for (String pathSplit : pathArr) {
            if (StringUtils.isNumeric(pathSplit)) {
                tr069Name.append("{i}.");
            } else {
                tr069Name.append(pathSplit).append(".");
            }
        }
        tr069Name.setLength(tr069Name.length()-1);

        // Check exist str069 name
        ParameterDetail[] parameterDetails = parameterDetailClient.findByTr069Name(String.valueOf(tr069Name));
        if(!ObjectUtils.empty(parameterDetails) && parameterDetails.length > 0) {
            check = Boolean.valueOf(parameterDetails[0].access);
        }

        return check;
    }

}
