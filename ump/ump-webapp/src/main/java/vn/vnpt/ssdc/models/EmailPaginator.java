package vn.vnpt.ssdc.models;

import vn.vnpt.ssdc.api.client.EmailTemplateClient;
import vn.vnpt.ssdc.api.model.EmailTemplat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lamborgini on 6/7/2017.
 */
public class EmailPaginator extends Paginator<EmailTemplat> {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final String PAGE_SIZE = "limit";
    public static final String PAGE_QUERY = "query";
    public static final String PAGE_OFFSET = "offset";
    public static final String PAGE_PARAM = "parameters";
    public static final String PAGE_CURRENT = "indexPage";
    public Map<String, String> emailQueryParam = new HashMap<>();
    public Map<String, String> indexParams = new HashMap<>();
    public int lastPage;
    public int currentPage;
    public int pageSize = DEFAULT_PAGE_SIZE;

    public EmailTemplateClient emailTemplateClient;
    public List<EmailTemplat> emailList = new ArrayList<EmailTemplat>();

    public EmailPaginator() {
        this.currentPage = 1;
    }

    public void parseParam(Map<String, String> requestParams) {
        String queryInput = null;
        this.number++;
        if (requestParams != null && !requestParams.isEmpty()) {

            if (requestParams.containsKey(PAGE_CURRENT) && requestParams.get(PAGE_CURRENT) != null) {
                this.currentPage = Integer.parseInt(requestParams.get(PAGE_CURRENT));
                this.number = Integer.parseInt(requestParams.get(PAGE_CURRENT));
            }
            if (requestParams.containsKey(PAGE_SIZE) && requestParams.get(PAGE_SIZE) != null) {
                if (!requestParams.get(PAGE_SIZE).toString().equals("0"))
                    this.pageSize = Integer.parseInt(requestParams.get(PAGE_SIZE));
            }
            if (requestParams.containsKey(PAGE_QUERY) && requestParams.get(PAGE_QUERY) != null) {
                queryInput = requestParams.get(PAGE_QUERY).toString();

            }

        }
        this.emailQueryParam.put(PAGE_PARAM, String.join(",", this.indexParams.keySet()));
        this.emailQueryParam.put(PAGE_SIZE, String.valueOf(this.pageSize));
        if (queryInput != null) {
            this.emailQueryParam.put(PAGE_QUERY, queryInput);
        }
        int offset = (this.currentPage - 1) * this.pageSize;
        this.emailQueryParam.put(PAGE_OFFSET, String.valueOf(offset));
        this.emailQueryParam.put(PAGE_CURRENT, currentPage != 0 ? String.valueOf(currentPage - 1) : "0");

    }

    public void loadResult(Map<String, String> requestParams) {

        EmailTemplat[] emailTemplats = emailTemplateClient.findEmail(emailQueryParam);
        for (EmailTemplat role : emailTemplats) {
            emailList.add(role);
        }
        this.totalPages = emailTemplateClient.findAll().length;

        if (this.totalPages == 0) {
            this.lastPage = 1;
        } else {
            int page = this.totalPages % (requestParams.size() == 0 ? DEFAULT_PAGE_SIZE
                    : Integer.valueOf(requestParams.get(PAGE_SIZE)));
            int pageTotal = this.totalPages / (requestParams.size() == 0 ? DEFAULT_PAGE_SIZE
                    : Integer.valueOf(requestParams.get(PAGE_SIZE)));
            this.lastPage = page == 0 ? pageTotal : pageTotal + 1;
        }


    }
    
}
