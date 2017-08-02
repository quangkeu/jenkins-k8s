$(document).ready(function() {
    $('.select').select2();
    $(":file").filestyle('icon', false);
    var totalDeviceBeChecked = 0;
    var deleteIds = [];
    var deleteNames = [];
    var hasFile = false;
    var resultSize = false;
    var maxSizeFile = false;
    var isValid = false;
    var idEditFile = '';

    var versionOld = '';
    var manufactureOld = '';
    var modelOld = '';
    var fileTypeOld = '';

    var paginator_data = $('#data-file-management-paginator');
    var indexPage = parseInt(paginator_data.attr('data-number'));
    var lastPagesVar = parseInt(paginator_data.attr('data-lastPage'));
    var startPageVar = parseInt(paginator_data.attr('data-number'));
    var limitPageInput = parseInt($('.limit-page-input').val());
    var input = $("#inputFile");
    $(".file-styled").uniform({
        fileButtonClass: 'action btn btn-default'
    });

    $('#btnCancel').click(function() {
        $("#version").val('');
        $("#manufacture").select2("val", "");
        $("#modelName").select2("val", "");
        $("#fileType").select2("val", "");

        $('#inputFile').wrap('<form>').closest('form').get(0).reset();
        $('#inputFile').unwrap();
        $(":file").filestyle('clear');
        $('#add-new-file-dialog').dialog('close');
    });

    $('#add-new-file-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: '50%',
        height: 'auto'
    });

    $('#newFile').click(function() {
        idEditFile = '';
        versionOld = '';
        manufactureOld = '';
        modelOld = '';
        fileTypeOld = '';
        $('#add_alert_danger').hide();

        $("#updateFileBtn").hide();
        $("#uploadFileBtn").show();
        $(":file").filestyle('placeholder', "No File");
        $('.ui-dialog-title').html('Add new file');
        $('#add-new-file-dialog').dialog('open');
    });

    $('.file-edit-opener').click(function() {
        idEditFile = $(this).attr('data-filename');
        manufacturer = $(this).attr('data-manufacturer');
        modelName = $(this).attr('data-modelName');
        fileType = $(this).attr('data-fileType');
        version = $(this).attr('data-version');
        filename = $(this).attr('data-uploadFileName');

        versionOld = version;
        manufactureOld = manufacturer;
        modelOld = modelName;
        fileTypeOld = fileType;

        $("#idEditFile").val(idEditFile);
        $("#version").val(version);
        $('#manufacture').val(manufacturer).trigger('change');
        $('#modelName').val(modelName).trigger('change');
        $('#fileType').val(fileType).trigger('change');
        $(":file").filestyle('placeholder', filename);

        $("#uploadFileBtn").hide();
        $("#updateFileBtn").show();
        $('#add_alert_danger').hide();
        $('.ui-dialog-title').html('Edit file');
        $('#add-new-file-dialog').dialog('open');
    });

    $('#updateFileBtn').click(function (event) {
        uploadFile();
    });

    $("#fileTypeSearch").change(function(){
        var fileTypeDataVar = $(this).val();
        var modelNameVar = $("#modelNameData").val();
        var manufacturerDataVar = $("#manufacturerData").val();
        searchFiles(manufacturerDataVar,modelNameVar,fileTypeDataVar);
    });
    
    function searchFiles(manufacturer, modelName,fileType) {
        var tmp;
        var totalParam = '';
        var startPageVar = parseInt(paginator_data.attr('data-number'));
        var limitPageInput = parseInt($('.limit-page-input').val());
        if(isNaN(limitPageInput)){
            limitPageInput = '20';
        }
        totalParam = "/file_management/search?"
            +"indexPage=1&limit="+limitPageInput;
        if(manufacturer != null && manufacturer != ""){
            totalParam += "&Manufacturer=" + manufacturer;
        } else {
              $("#fileTypeSearch").val('');
              fileType = '';
              $("#modelNameData").val('');
              modelName = '';
        }
        if(modelName != null && modelName != ""){
            totalParam += "&ModelName=" + modelName
        } else {
            $("#fileTypeSearch").val('');
            fileType = '';
        }
        if(fileType != null && fileType != ""){
            totalParam += "&FileType=" + fileType
        }

        window.location.href = totalParam;
     }
    $('#confirm-delete-file-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: '50%',
        height: 'auto'
    });
    $('#confirm-delete-file').click(function() {
        deleteNamesDisplay = [];
        $('.myCheckBox').each(function() {
            var uploadFileName = $(this).attr('data-uploadFileName');
            var id = $(this).attr('data-id');
            var filename = $(this).attr('data-filename');
            if ($(this).is(':checked')) {
                deleteIds.push(id);
                hasFile = true;
                deleteNamesDisplay.push(uploadFileName);
                deleteNames.push(filename);
            }
        });
        $('.ui-dialog-title').html('Are you sure you want to delete : ' + deleteNamesDisplay + ' ?');
        $('#confirm-delete-file-dialog').dialog('open');
    });
    $('#confirm-delete-file-yes-dialog').click(function() {
        if (hasFile == false) {

        } else {
            $.ajax({
                type: 'POST',
                url: "/file_management/delete/",
                data: {
                    deleteIds: deleteIds+"@",
                    deleteNames: deleteNames+"@",
                },
                success: function(result) {
                    window.location = "/file_management";
                },
                error: function(result) {
                },
                async: true
            });
        }
        $('#confirm-delete-file-dialog').dialog('close');
    });
    $('#confirm-delete-file-no-dialog').click(function() {
        $('#confirm-delete-file-dialog').dialog('close');
    });

    function bytesToSize(bytes, precision)
    {
        var kilobyte = 1024;
        var megabyte = kilobyte * 1024;
        var gigabyte = megabyte * 1024;
        var terabyte = gigabyte * 1024;

        if ((bytes >= 0) && (bytes < kilobyte)) {
            return bytes + ' B';

        } else if ((bytes >= kilobyte) && (bytes < megabyte)) {
            return (bytes / kilobyte).toFixed(precision) + ' KB';

        } else if ((bytes >= megabyte) && (bytes < gigabyte)) {
            return (bytes / megabyte).toFixed(precision) + ' MB';

        } else if ((bytes >= gigabyte) && (bytes < terabyte)) {
            return (bytes / gigabyte).toFixed(precision) + ' GB';

        } else if (bytes >= terabyte) {
            return (bytes / terabyte).toFixed(precision) + ' TB';

        } else {
            return bytes + ' B';
        }
    }

    $('#inputFile').bind('change', function() {
        if(this.files[0] != null){
            var size = this.files[0].size;
            var sizeInMB = (size / (1024*1024)).toFixed(2);
            if(parseInt(sizeInMB) < 1024){
                var filename = $('input[type=file]').val().split('\\').pop();
                if(filename.indexOf(' ') === -1){
                    resultSize = true;
                    maxSizeFile = true;
                    $('#add_alert_danger').hide();
                } else {
                    resultSize = false;
                    $(":file").filestyle('clear');
                    $('#add_alert_danger span.text-bold').text("Remove whitespace characters in filenames !");
                    $('#add_alert_danger').show()
                }
            } else {
                resultSize = false;
                maxSizeFile = false;
                if(idEditFile == ''){
                    $(":file").filestyle('clear');
                }
                $('#add_alert_danger span.text-bold').text("Maximum file size is 20 mb !");
                $('#add_alert_danger').show();
            }
        }
    });


    $("#add-new-file-dialog-form").on("submit", function(event) {
        event.preventDefault();
        check = $("#add-new-file-dialog-form").valid();
        if(check){
            uploadFile();
        }
    });


    function uploadFile(){
        var version = $("#version").val();
        var manufacturer = $('#manufacture').val();
        var modelName = $('#modelName').val();
        var fileType = $('#fileType').val();
        var filename = $('input[type=file]').val().split('\\').pop();

        resultEdit = validateEditFile(manufacturer, modelName, version);
        console.log('resultEdit : '+resultEdit);

        if(idEditFile != ''){
            if(resultEdit){
                fire_ajax_submit();
            } else {
                checkExistedVersion(manufacturer, modelName, version, fileType, idEditFile);
            }
        } else {
                checkExistedVersion(manufacturer, modelName, version, fileType, idEditFile);
        }

    }

    function checkExistedVersion(manufacturer, modelName, version, fileType, idEditFile){
        $.ajax({
            type: 'POST',
            url: "/file_management/existed/",
            data: {
                version: version,
                manufacturer: manufacturer,
                modelName: modelName,
                fileType: fileType,
                idEditFile: idEditFile,
            },
            success: function(result) {
                if(result == false){
                    $('#add_alert_danger span.text-bold').text("Existed version : "+version+" !");
                    $('#add_alert_danger').show();
                    $("#version").val("");
                } else {
                    $('#add_alert_danger').hide();
                    fire_ajax_submit();
                }
            },
            error: function(data) {
            },
            async: true
        });
    }

    function validateEditFile(manufacturer, modelName, version){

        if(manufactureOld != manufacturer){
            return false;
        }
        if(modelOld != modelName){
            return false;
        }
        if(versionOld != version){
            return false;
        }
        return true;
    }



    function validate(version, manufacturer, modelName, fileType){

        if(manufacturer == null || manufacturer == ''){
            resultSize = false;
        } else {resultSize = true;}
        if(modelName == null || modelName == ''){
            resultSize = false;
        } else {resultSize = true;}
        if(fileType == null || fileType == ''){
            resultSize = false;
        } else {resultSize = true;}
        if(version == ''){
            resultSize = false;
        } else {resultSize = true;}
        if(idEditFile == ''){
            var filename = $('input[type=file]').val().split('\\').pop();
            if(filename== null || filename == '') {
                 resultSize = false;
            } else {
                resultSize = true;
            }
        }

    }

    function fire_ajax_submit() {
        var form = $('#add-new-file-dialog-form')[0];
        var data = new FormData(form);
        $("#uploadFileBtn").prop("disabled", true);
        $.ajax({
            type: "POST",
            enctype: 'multipart/form-data',
            url: "/file_management/addNewFile",
            data: data,
            processData: false,
            contentType: false,
            cache: false,
            timeout: 60000,
            success: function (data) {
                $("#btnSubmit").prop("disabled", false);
                if(data){
                    window.location = "/file_management";
                }
            },
            error: function (e) {
                $("#btnSubmit").prop("disabled", false);
            }
        });
    }


    $('#checkAll').change(function() {
        if ($(this).is(":checked")) {
            checkAll();
        } else {
            unCheckAll();
        }

    });

    $('.myCheckBox').change(function() {
        if ($(this).is(":checked")) {
            totalDeviceBeChecked = totalDeviceBeChecked + 1;
        } else {
            totalDeviceBeChecked = totalDeviceBeChecked - 1;
        }

        if (totalDeviceBeChecked > 0) {
            showGroupButton(true);
        } else {
            showGroupButton(false)
        }
    });
    $('.myCheckBox').each(function() {
        if ($(this).is(':checked')) {
            totalDeviceBeChecked += 1;
        }
    });
    if (totalDeviceBeChecked > 0) {
        showGroupButton(true);
    } else {
        showGroupButton(false)
    };
    function checkAll() {
        totalDeviceBeChecked =0;
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            $('#' + id).prop('checked', true);
           totalDeviceBeChecked++;
        });
        showGroupButton(true);
    }
    function unCheckAll() {
        totalDeviceBeChecked = 0;
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            $('#' + id).prop('checked', false);
        });
        showGroupButton(false);
    }
    function showGroupButton(enable) {
        if (enable === true) {
            $('#confirm-delete-file').removeClass("disabled");
        } else {
            $('#confirm-delete-file').addClass("disabled");
        }
    }

    $('.twbs-prev-next').twbsPagination({
        totalPages: lastPagesVar,
        visiblePages: 4,
        startPage: startPageVar,
        prev: '&#8672;',
        next: '&#8674;',
        first: '&#8676;',
        last: '&#8677;',
        onPageClick: function (event, page) {
            if(page !== parseInt(paginator_data.attr('data-number'))) {
                var limitPageInput = parseInt($('.limit-page-input').val());
                pagingDataModel(page, limitPageInput);

            }
        }
    });
    $('.go-page-btn').click(function () {
        var goPageInputVar = $('.go-page-input').val();
        if(goPageInputVar.indexOf("-") >= 0
            || goPageInputVar.indexOf("+") >= 0 || goPageInputVar.indexOf(".") >= 0
            || goPageInputVar == ""){
                $('#alert_danger span.text-bold').text("Invalid Number!");
                $('#alert_danger').show();
        } else {
            var goPageInput = parseInt($('.go-page-input').val());
            var limitPageInput = parseInt($('.limit-page-input').val());
            if(0 < goPageInput && goPageInput <= lastPagesVar){
                pagingDataModel(goPageInput, limitPageInput);
            } else {
                $('#alert_danger span.text-bold').text("Invalid Number!");
                $('#alert_danger').show();
            }
        }

    });
    $('.limit-page-input').change(function () {
        var limitPageInput = parseInt($('.limit-page-input').val());
        var indexPage = parseInt(paginator_data.attr('data-number'));
        pagingDataModel(1, limitPageInput);

    });
    function pagingDataModel(index, limit) {
            var _manufacturer = $("#manufacturerData").val();
            var _modelName = $("#modelNameData").val();
            var _fileTypeSearch = $("#fileTypeSearch").val();
            var totalParam = '';
            totalParam = "/file_management/search?indexPage=" + index + "&limit=" + limit;

            if(_manufacturer != ''){
                totalParam += "&Manufacturer=" + _manufacturer;
            }
            if(_modelName != ''){
                totalParam += "&ModelName="+ _modelName;
            }
            if(_fileTypeSearch != ''){
                totalParam += "&FileType=" + _fileTypeSearch;
            }
            window.location.href = totalParam;
    }


    $("#manufacture").change(function(){
        var manufacturerDataVar = $(this).val();
        var resultJSON = $("#addNewFileParam").val();
        var result = JSON.parse(resultJSON);
        $('#modelName').html('').select2({data: [{id: '', text: ''}]});
        var dataArray = [];
        $.each(result, function(k, v) {
            if(manufacturerDataVar == k){
                $.each(v, function( index, value ) {
                    dataArray.push({id: value, text: value});
                });
            }
        });
        $("#modelName").html('').select2({data: dataArray});
    });


    $("#manufacturerData").change(function(){
        var manufacturerDataVar = $(this).val();
        var fileTypeDataVar = $("#fileTypeSearch").val();
        searchFiles(manufacturerDataVar,"", fileTypeDataVar);
    });

    $("#modelNameData").change(function(){
        var modelNameVar = $(this).val();
        var manufacturerDataVar = $("#manufacturerData").val();
        var fileTypeDataVar = $("#fileTypeSearch").val();
            searchFiles(manufacturerDataVar,modelNameVar,fileTypeDataVar);
    });

});