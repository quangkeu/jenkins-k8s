$(document).ready(function() {
    var output = JSON.parse($('#data-tree-permissions').val());
    var listClass = '';
    var listParameterObjectChecked = '';
    var addNameRole = $('#add-name-role');
    var addDescriptionRole = $('#add-description-role');
    var totalPermissions = '';
    var isValid = true;
    var alertText='';
    var permissionID='';
    var roleNameOld = '';
    var roleNameOldCheck = 0;

    $("#tree").fancytree({
        source: output,
        checkbox: true,
        folder : false,
        selectMode : 3,
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

            listParameterObjectChecked += '@' + node.key;

            listTemp = $.map(data.tree.getSelectedNodes(true), function(node){
                            return '~'+node.key;
            });
            listParameterObjectChecked = listTemp;
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

    $('#update-role-btn-save').hide();
//    $('#update-role-btn-save').addClass("disabled");

    $('#add-role-btn-reset').click(function() {
        $('#add-name-role').val("");
        $('#add-description-role').val("");
        $("#tree").fancytree("getTree").visit(function(node){
                node.setSelected(false);
        });
//        $('#update-role-btn-save').unbind( "click" );
//        $('#update-role-btn-save').addClass("disabled");
        $('#add-role-btn-save').show();
        $('#update-role-btn-save').hide();
//        $('#add-role-btn-save').removeClass("disabled");
        $('#add-name-role').removeClass("disabled");

        listParameterObjectChecked = '';
    });

    $('#add-role-btn-save').click(function() {
        validateRole();
        if(isValid){
            totalPermissions = listAllPermissions(listParameterObjectChecked);
            $.ajax({
            type: 'POST',
            url: "/addOrEditRole",
            data: {
                roleNameOldCheck: "0",
                addNameRole: addNameRole.val(),
                addDescriptionRole: addDescriptionRole.val(),
                addOperationRole: "",
                addPermission: totalPermissions+'*'
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

    function validateRole(){
        if(listParameterObjectChecked == ''){
            isValid = false;
            alertText = "Permission is not empty";
            return;
        }
        name = addNameRole.val();
        if(name != '' && name.indexOf(' ') == -1 && check(name) == false
                && name.length > 5 && name.length < 21){
            isValid = true;
            $('#alert_danger').hide();
            return;
        } else {
            if(name == '' || name.indexOf(' ') >= 0 || name.length < 5 || name.length > 21){
                if(name.length < 5 || name.length > 21){
                    alertText = "Please enter at least 5 characters.";
                } else {alertText = "Invalid role";}
                isValid = false;
            } else {
                isValid = false;
                alertText = "Group Name contains illegal characters.";
            }
        }
    }

    function listAllPermissions(listParameterObjectChecked){
        var totalParam = '';
        var listChecked = listParameterObjectChecked.toString().substring(1);
        var operationIdsArray = [];
        if(listChecked.indexOf(',~') > -1){
            operationIdsArray = listChecked.split(',~');
        } else {
            operationIdsArray.push(listChecked);
        }

        var tree = $("#tree").fancytree("getTree");
        var d = tree.toDict(true);
        var d1 = jQuery.parseJSON(JSON.stringify(d));
        for(var i=0; i<operationIdsArray.length; i++){
            if(operationIdsArray[i].indexOf('-') == -1){
                // node
                for(var j=0; j<d1.children.length; j++) {
                    if(d1.children[j].key == operationIdsArray[i]){
                        var rootFather = d1.children[j].children;
                        for(var k=0; k<rootFather.length; k++) {
                            totalParam += '@'+rootFather[k].key;
                        }
                    }
                }
            } else {
                // child
                totalParam += '@'+operationIdsArray[i];
            }
        }
        totalParam = totalParam.substring(1);
        return totalParam;
    }

    $('#update-role-btn-save').click(function() {
        if(roleID != ''){
            validateRole();
            if(isValid){
                totalPermissions = listAllPermissions(listParameterObjectChecked);

                if(roleNameOld == addNameRole.val()){
                    roleNameOldCheck = 1;
                }

                $.ajax({
                type: 'POST',
                url: "/addOrEditRole",
                data: {
                    roleID: roleID,
                    roleNameOldCheck: roleNameOldCheck,
                    addNameRole: addNameRole.val(),
                    addDescriptionRole: addDescriptionRole.val(),
                    addPermission: totalPermissions+'*'
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

    $('.role-edit-opener').click(function() {
        $('#add-role-btn-save').hide();
        $('#update-role-btn-save').show();

//        $('#update-role-btn-save').bind( "click" );
//        $('#update-role-btn-save').removeClass("disabled");
//        $('#add-role-btn-save').unbind( "click" );
//        $('#add-role-btn-save').addClass("disabled");
        roleID = $(this).attr('data-role');
        permissionID = $(this).attr('data-role');
        permissionsIds = $(this).attr('data-permissionsIds');
        permissionsIds = permissionsIds.substring(1,permissionsIds.length - 1);
        var permissionsIdsArray = permissionsIds.trim().split(',');

        roleNameEdit = $(this).attr('data-roleName');
        if(roleNameEdit == 'SuperAdmin'){
            $('#add-name-role').attr('disabled', 'disabled');
        } else {$('#add-name-role').removeAttr('disabled', 'disabled');}
        roleNameOld = roleNameEdit;
        addNameRole.val(roleNameEdit);
        addDescriptionRole.val($(this).attr('data-description'));

        var treeData1 = jQuery.parseJSON(JSON.stringify(treeData1Copy));
        for(var i=0; i<treeData1.children.length; i++) {
            var rootFather = treeData1.children[i].children;
            for(var j=0; j<rootFather.length; j++) {
                var temp = rootFather[j].key.trim().split('-');
                $.each(permissionsIdsArray, function (index, value) {
                    if(parseInt(temp[0]) == parseInt(value)){
                        rootFather[j].selected = true;
                        listParameterObjectChecked += "#" + rootFather[j].key;
                    }
                });
            }
        }
        tree.reload(treeData1);
        treeData1 = jQuery.parseJSON(JSON.stringify(treeData));

    });



    var paginator_data = $('#data-role-paginator');
    var indexPage = parseInt(paginator_data.attr('data-number'));
    var lastPagesVar = parseInt(paginator_data.attr('data-lastPage'));
    var startPageVar = parseInt(paginator_data.attr('data-number'));
    var limitPageInput = parseInt($('.limit-page-input').val());
    var totalRoleBeChecked = 0;
    var confirmDeleteRole = $('#delete_multi_role');
    var confirmDeleteRole_html = confirmDeleteRole.html();
    var roleDeleteId = '';
    var deleteIds = [];
    var deleteParams = [];
    var hasDevice = false;
    var groupNameSingle = '';

    confirmDeleteRole.addClass("disabled");
    confirmDeleteRole.unbind( "click" );
    $('#checkAll').change(function() {
        if ($(this).is(":checked")) {
            checkAll();
        } else {
            unCheckAll();
        }
    });

    $('.myCheckBox').change(function() {
        if ($(this).is(":checked")) {
            totalRoleBeChecked = totalRoleBeChecked + 1;
        } else {
            totalRoleBeChecked = totalRoleBeChecked - 1;
        }

        if (totalRoleBeChecked > 0) {
            showGroupButton(true);
        } else {
            showGroupButton(false)
        }
    });

    $('.myCheckBox').each(function() {
        if ($(this).is(':checked')) {
            totalRoleBeChecked++;
        } else {
            if(totalRoleBeChecked != 0){totalRoleBeChecked--;}
        }
    });

    if (totalRoleBeChecked > 0) {
        showGroupButton(true);
    } else {
        showGroupButton(false)
    };

    function checkAll() {
        totalRoleBeChecked =0;
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            $('#' + id).prop('checked', true);
           totalRoleBeChecked++;
        });
        showGroupButton(true);
    }

    function unCheckAll() {
        totalRoleBeChecked = 0;
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            $('#' + id).prop('checked', false);
        });
        showGroupButton(false);
    }

    function showGroupButton(enable) {
        if (enable) {
            confirmDeleteRole.bind( "click" );
            confirmDeleteRole.removeClass("disabled");
            confirmDeleteRole.html(confirmDeleteRole_html + ' ('+totalRoleBeChecked+')');
        } else {
            confirmDeleteRole.html(confirmDeleteRole_html);
            confirmDeleteRole.addClass("disabled");
            confirmDeleteRole.unbind( "click" );
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
        window.location.href = "/role/search?indexPage=" + index + "&limit=" + limit;
    }

    $('#confirm-delete-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: '50%',
        height: 'auto'
    });
    $('#confirm-delete-now-dialog').click(function() {
        if(deleteIds != ''){
            $.ajax({
                type: 'POST',
                url: "/deleteRole",
                data: {
                    deleteIds: deleteIds+"@",
                    deleteNames: deleteParams+"@"
                },
                success: function(response) {
                console.log('response : '+response);
                        if(response == 'success'){
                            roleDeleteId = '';
                            location.reload();
                        } else if( response == 'fail'){
                            $('#confirm-delete-dialog').dialog('close');
                            $('#alert_danger_delete span.text-bold').text("Delete fail !");
                            $('#alert_danger_delete').show();
                        }
                },
                async: true
            });
        }

    });
    $('#confirm-delete-dialog-close').click(function() {
        $('#confirm-delete-dialog').dialog('close');
    });
    $('.role-delete-opener').click(function() {
        roleDeleteId = $(this).attr('data-roleId');
        roleDeleteName = $(this).attr('data-name');
        if(roleDeleteName != "SuperAdmin"){
            $('#alert_danger_delete').hide();
            deleteIds = [];
            deleteParams = [];
            deleteIds.push(roleDeleteId);
            deleteParams.push(roleDeleteName);
            $('.ui-dialog-title').html('Do you want to delete ' + roleDeleteName +' ?');
            $('#confirm-delete-dialog').dialog('open');
        } else {
            $('#alert_danger_delete').show();
        }

    });


    $('#delete_multi_role').click(function() {
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