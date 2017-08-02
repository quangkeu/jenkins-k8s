package vn.vnpt.ssdc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.vnpt.ssdc.api.client.BackupFileClient;
import vn.vnpt.ssdc.models.BackupFilePaginator;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lamborgini on 6/8/2017.
 */
@Controller
public class BackupFileController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(BackupFileController.class);
    private static final String BACKUP_FILE_PAGE = "backup_file/backup_file";

    @Autowired
    private BackupFileClient backupFileClient;

    @GetMapping("/backup_file")
    public String index(Model model, @RequestParam Map<String, String> requestParams) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to backup file page","","");
        loadDataIndex(model, requestParams,"index");
        return BACKUP_FILE_PAGE;
    }

    @GetMapping("/backup_file/search")
    public String search(Model model, @RequestParam Map<String, String> requestParams) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"search on backup file page","","");
        loadDataIndex(model, requestParams, "search");
        return BACKUP_FILE_PAGE;
    }

    private void loadDataIndex(Model model, Map<String, String> requestParams, String mode) {
        Map<String, String> fileManagementIndexParams = backupFileIndexParams();
        BackupFilePaginator backupFilePaginator = new BackupFilePaginator();
        backupFilePaginator.backupFileClient = backupFileClient;
        backupFilePaginator.indexParams = fileManagementIndexParams;
        backupFilePaginator.parseParam((HashMap<String, String>) requestParams);
        backupFilePaginator.loadResult(requestParams);

        model.addAttribute("paginator", backupFilePaginator);
//        loadDataSearch(model, requestParams);
    }
}
