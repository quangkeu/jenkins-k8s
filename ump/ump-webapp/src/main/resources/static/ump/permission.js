$(document).ready(function() {
    var output = JSON.parse($('#data-tree-permissions').val());
    var listParameterObjectChecked = '';
    var listClass = '';
    var addGroupName = $('#add-group-name');
    var addPermission = $('#add-permission');
    var addDescription = $('#add-description');
    var totalOperations = '';
    var isValid = true;
    var alertText='';
    var permissionID='';
    var groupNameEditOld = '';
    var groupNameEditOldCheck = 0;

    $("#tree").fancytree({
        source: output,
        checkbox: true,
        folder : false,
        selectMode : 3,
        activate: function(event, data){
                var node = data.node;
            },
        click: function (event, data) {
            var node = data.node;
        },
        select: function(event, data) {
            var node = data.node;
            if (node.isSelected()) {
                if (node.isUndefined()) {
                    // Load and select all child nodes
                    node.load().done(function() {
                        node.visit(function(childNode) {
                            childNode.setSelected(true);
                        });
                    });
                } else {
                    // Select all child nodes
                    node.visit(function(childNode) {
                        childNode.setSelected(true);
                    });
                }
            }
            listParameterObjectChecked = $.map(data.tree.getSelectedNodes(true), function(node){
                            return node.key;
             });
             listClass = $.map(data.tree.getSelectedNodes(true), function(node){
                             return node.countChildren(node.key);
              });
        },
//        loadChildren: function(event, ctx) {
//            ctx.node.fixSelection3AfterClick();
//        },

    });
    var node = $("#tree").fancytree("getRootNode");
    node.sortChildren(null, true);
    var specialChars = "<>@!#$%^&*()_+[]{}?:;|'\"\\,./~`-=";
    var check = function(string){
        for(i = 0; i < specialChars.length;i++){
            if(string.indexOf(specialChars[i]) > -1){
                return true
            }
        }
        return false;
    }

    $('#update-permission-btn-save').hide();

    $('#add-permission-btn-reset').click(function() {
        $('#add-group-name').val("");
        $('#add-permission').val("");
        $('#add-description').val("");
        $("#tree").fancytree("getTree").visit(function(node){
                node.setSelected(false);
        });
        $('#add-permission-btn-save').show();
        $('#update-permission-btn-save').hide();
        $('#add-group-name').removeAttr('disabled', 'disabled');
        listParameterObjectChecked = '';
    });

    $('#add-permission-btn-save').click(function() {
        validatePermission();
        if(isValid){
            totalOperations = listAllOperations(listParameterObjectChecked);

            $.ajax({
                type: 'POST',
                url: "/addOrEditPermission",
                data: {
                    groupNameOldCheck: '0',
                    addGroupName: addGroupName.val(),
                    addPermission: addPermission.val(),
                    addDescription: addDescription.val(),
                    addOperations: totalOperations+'@'
                },
                success: function(response) {
                    if(response == 'success'){
                        listParameterObjectChecked = '';
                        location.reload();
                    } else {
                        $('#alert_danger span.text-bold').text(response+"");
                        $('#alert_danger').show();
                    }
                },
                async: true
            });
        } else {
            if(alertText != ''){
                $('#alert_danger span.text-bold').text(alertText+"");
                $('#alert_danger').show();
            }

        }

    });

    function validatePermission(){
        if(listParameterObjectChecked == ''){
            isValid = false;
//            alertText = "Invalid Operations";
            return;
        }

        if(addPermission.val() == ''){
            isValid = false;
            return;
//            alertText = "Invalid Permission";
        }
        name = addGroupName.val();
        if(name != '' && check(name) == false && name.indexOf(' ') == -1
                && name.length > 5 && name.length < 21){
            isValid = true;
            $('#alert_danger').hide();
            return;
        } else {
            if(name == '' || name.indexOf(' ') == -1 || name.length < 5 || name.length > 21){
                console.log(name.length);
                if(name.length < 5 || name.length > 21){
                    alertText = "Please enter at least 5 characters.";
                } else {alertText = "Invalid permission";}
                isValid = false;
            } else {
                isValid = false;
                alertText = "Group Name contains illegal characters.";
            }
        }
    }

    function listAllOperations(listParameterObjectChecked){
        var totalParam = '';
        var operationIdsArray = listParameterObjectChecked.toString().split(',');
        var listClassArray = listClass.toString().split(',');

        var tree = $("#tree").fancytree("getTree");
        var d = tree.toDict(true);
        var d1 = jQuery.parseJSON(JSON.stringify(d));
        for(var i=0; i<operationIdsArray.length; i++){
            if(typeof listClassArray[i] != 'undefined' && listClassArray[i] != 0){
                // node
                for(var j=0; j<d1.children.length; j++) {
                    if(d1.children[j].key == operationIdsArray[i]){
                        var rootFather = d1.children[j].children;
                        for(var k=0; k<rootFather.length; k++) {
                            if(rootFather[k].key != ''){
                                totalParam += ','+rootFather[k].key;
                            }
                        }
                    }
                }
            } else {
                // child
                if(operationIdsArray[i] != ''){
                    totalParam += ','+operationIdsArray[i];
                }
            }
        }
        totalParam = totalParam.substring(1);
        return totalParam;
    }

    $('#update-permission-btn-save').click(function() {
        if(permissionID != ''){
            validatePermission();
            if(isValid){
                totalOperations = listAllOperations(listParameterObjectChecked);

                if(groupNameEditOld == addGroupName.val()){
                    groupNameEditOldCheck = 1;
                }

                $.ajax({
                type: 'POST',
                url: "/addOrEditPermission",
                data: {
                    groupNameOldCheck : groupNameEditOldCheck,
                    permissionID: permissionID,
                    addGroupName: addGroupName.val(),
                    addPermission: addPermission.val(),
                    addDescription: addDescription.val(),
                    addOperations: totalOperations+'@'
                },
                success: function(response) {
                    if(response == 'success'){
                        listParameterObjectChecked = '';
                        location.reload();
                    } else {
                        $('#alert_danger span.text-bold').text(response+"");
                        $('#alert_danger').show();
                    }
                },
                async: true
                });
            } else {
                if(alertText != ''){
                    $('#alert_danger span.text-bold').text(alertText+"");
                    $('#alert_danger').show();
                }

            }
        }
    });


    var tree = $("#tree").fancytree("getTree");
    var treeData = tree.toDict(true);
    var treeData1Copy = treeData;
    $('.permission-edit-opener').click(function() {
        $('#add-permission-btn-save').hide();
        $('#update-permission-btn-save').show();
        listParameterObjectChecked = '';
        permissionID = $(this).attr('data-permission');
        operationIds = $(this).attr('data-operationIds');
        operationIds = operationIds.substring(1,operationIds.length - 1);
        var operationIdsArray = operationIds.trim().split(',');

        groupNameEdit = $(this).attr('data-groupName');
        if(groupNameEdit == 'SuperAdmin'){
            $('#add-group-name').attr('disabled', 'disabled');
        } else {$('#add-group-name').removeAttr('disabled', 'disabled');}
        groupNameEditOld = groupNameEdit;
        addGroupName.val(groupNameEdit);
        addPermission.val($(this).attr('data-permissionName'));
        addDescription.val($(this).attr('data-description'));

        var treeData1 = jQuery.parseJSON(JSON.stringify(treeData1Copy));
        for(var i=0; i<treeData1.children.length; i++) {
            var rootFather = treeData1.children[i].children;
            for(var j=0; j<rootFather.length; j++) {
                $.each(operationIdsArray, function (index, value) {
                    if(value.trim() == rootFather[j].key){
                        rootFather[j].selected = true;
                        listParameterObjectChecked += ',' + rootFather[j].key;
                    }
                });
            }
        }

        tree.reload(treeData1);
        treeData1 = jQuery.parseJSON(JSON.stringify(treeData));
    });

    var paginator_data = $('#data-permission-paginator');
    var indexPage = parseInt(paginator_data.attr('data-number'));
    var lastPagesVar = parseInt(paginator_data.attr('data-lastPage'));
    var startPageVar = parseInt(paginator_data.attr('data-number'));
    var limitPageInput = parseInt($('.limit-page-input').val());
    var totalDeviceBeChecked = 0;
    var confirmDeletePermission = $('#delete_multi_permission');
    var confirmDeletePermission_html = confirmDeletePermission.html();
    var permissionDeleteId = '';
    var deleteIds = [];
    var deleteParams = [];
    var hasDevice = false;
    var modeDelete = '';
    var groupNameSingle = '';

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
            totalDeviceBeChecked++;
        } else {
            if(totalDeviceBeChecked != 0){totalDeviceBeChecked--;}
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
        if (enable) {
            confirmDeletePermission.bind( "click" );
            confirmDeletePermission.removeClass("disabled");
            confirmDeletePermission.html(confirmDeletePermission_html + ' ('+totalDeviceBeChecked+')');
        } else {
            confirmDeletePermission.html(confirmDeletePermission_html);
            confirmDeletePermission.addClass("disabled");
            confirmDeletePermission.unbind( "click" );
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
        if(isNaN(index)){
            index = '1';
        }
        if(isNaN(limit)){
            limit = '20';
        }
        window.location.href = "/permission/search?indexPage=" + index + "&limit=" + limit;
    }

    $('#confirm-delete-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: '50%',
        height: 'auto'
    });
    $('#confirm-delete-now-dialog').click(function() {
        console.log('deleteIds : '+deleteIds);
        if(deleteIds != ''){
            $.ajax({
                type: 'POST',
                url: "/deletePermission",
                data: {
                    deleteIds: deleteIds+"@",
                    deleteNames: deleteParams + "@"
                },
                success: function(response) {
                    if(response == 'success'){
                        permissionDeleteId = '';
                        location.reload();
                    }
                },
                async: true
            });
        }

    });
    $('#confirm-delete-dialog-close').click(function() {
        $('#confirm-delete-dialog').dialog('close');
    });
    $('.permission-delete-opener').click(function() {
        permissionDeleteId = $(this).attr('data-permissionId');
        permissionDeleteName = $(this).attr('data-groupName');
        if(permissionDeleteName != "SuperAdmin"){
            deleteIds = [];
            deleteParams = [];
            deleteIds.push(permissionDeleteId);
            deleteParams.push(permissionDeleteName);
            $('.ui-dialog-title').html('Do you want to delete ' + permissionDeleteName +' ?');
            $('#confirm-delete-dialog').dialog('open');
        } else {
            $('#alert_danger_delete').show();
        }


    });


    $('#delete_multi_permission').click(function() {
        modeDelete = 'multi';
        deleteIds = [];
        deleteParams = [];
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            var name = $(this).attr('name');
            if ($(this).is(':checked')) {
                deleteIds.push(id);
                deleteParams.push(name);
            }
        });
        $('.ui-dialog-title').html('Do you want to delete '+ deleteParams +' ?');
        $('#confirm-delete-dialog').dialog('open');
    });

    $('#alert_danger_delete_close').click(function() {
        $('#alert_danger_delete').hide();
    });

});