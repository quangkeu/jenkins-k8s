package vn.vnpt.ssdc.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpt.ssdc.api.client.DiagnosticApiClient;
import vn.vnpt.ssdc.api.model.DiagnosticTask;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by thangnc on 10-Apr-17.
 */
public class DiagnosticPaginator  extends Paginator<DiagnosticTask> {

    public static final Logger logger = LoggerFactory.getLogger(DiagnosticTask.class);

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final String PAGE_SIZE = "limit";
    public static final String PAGE_CURRENT = "indexPage";


    public int currentPage;
    public int lastPage;
    public int pageSize = DEFAULT_PAGE_SIZE;
    public int offset = 1;
    public Map<String, String> indexParams = new HashMap<String, String>();


    public String description;
    public List<DiagnosticTask> diagnosticTasks;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public DiagnosticPaginator() {
        this.currentPage = 0;
    }

    public DiagnosticApiClient diagnosticApiClient;

    public void loadResult(Map<String, String> requestParams, String deviceId, String tab) {
        DiagnosticTask[] diagnosticTasks = null;
        if(requestParams.get(PAGE_CURRENT) != null && !("").equals(requestParams.get(PAGE_CURRENT)))  offset = Integer.parseInt(requestParams.get(PAGE_CURRENT));
        if(requestParams.get(PAGE_SIZE) != null && !("").equals(requestParams.get(PAGE_SIZE)))  pageSize = Integer.parseInt(requestParams.get(PAGE_SIZE));

        if (("").equals(tab)) {
            tab = "notAll";
        }

        diagnosticTasks = diagnosticApiClient.getAllTask(deviceId, offset, pageSize, tab);

        List<DiagnosticTask> diagnosticTaskList = new LinkedList<>();

        for (int i = 0; i < diagnosticTasks.length; i++) {
            try {
                DiagnosticTask diagnosticTask = diagnosticTasks[i];
                diagnosticTaskList.add(diagnosticTask);
                diagnosticTasks[i].parameters = new HashMap<String, String>() {{
                    put("Id", diagnosticTask.getId() + "");
                    put("Diagnostics Type", diagnosticTask.getDiagnosticsName());
                    put("Created", sdf.format(new Date(diagnosticTask.created)));
                    if (diagnosticTask.completed != null) put("Completed", sdf.format(new Date(diagnosticTask.completed)));
                }};
            } catch (Exception e) {}
        }

        this.diagnosticTasks = diagnosticTaskList;
        this.totalPages = diagnosticApiClient.getAllTask(deviceId, offset, pageSize, "all").length;


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

    public void parseParam(HashMap<String, String> requestParams) {
        this.number++;
        if (requestParams != null && !requestParams.isEmpty()) {

            if (isExits(requestParams, PAGE_CURRENT)) {
                this.currentPage = Integer.parseInt(requestParams.get(PAGE_CURRENT));
                this.number = Integer.parseInt(requestParams.get(PAGE_CURRENT));
            }
            if (isExits(requestParams, PAGE_SIZE) && !("0").equals(requestParams.get(PAGE_SIZE))) {
                this.pageSize = Integer.parseInt(requestParams.get(PAGE_SIZE));
            }
        }
    }

    public static boolean isExits(HashMap<String, String> requestParams, String key) {
        if (requestParams.containsKey(key) && requestParams.get(key) != null) {
            return true;
        }
        return false;
    }
}
