/**
 * Created by vietnq on 11/3/16.
 */
$(function(){

    // Device types tree
    $(".tree-default").fancytree({
        icon: false,
        click: function(event, data) {
            if(data.node.data.href && typeof data.node.data.href !== 'undefined') {
                window.location.replace(document.location.origin + data.node.data.href);
            }
        }
    });

    // File input style
    $(".file-styled").uniform({
        fileButtonClass: 'action btn btn-default'
    });

    $('#upload-data-model-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: 500
    });
    $('#upload-data-model-dialog-opener').click(function() {
        $('#upload-data-model-dialog').dialog('open');
        $('#showErrorFormat').hide();
    });

    //list of parameters in dom
    var params = $('button#create-tag-dialog-opener').attr('data-params');
    var objectParams = $('button#create-tag-dialog-opener').attr('data-object-params');
    var parentObjectValue = "";

    if(params !== undefined) {
        $("input.parameter-path").autocomplete({
            source: params.split(",")
        });
    }

    //append a row next to current edited row (in create tag dialog)
    var html = ''
        +'<div class="row tag-parameter-row" rowOrder="{i}">'
        +'  <div class="col-md-3">'
        +'      <input name="shortName_{i}" type="text" class="form-control" placeholder="Short name" required="required" unique="unique">'
        +'  </div>'
        +'  <div class="col-md-6">'
        +'      <select name="path_{i}" class="form-control parameter-path" required="required" data-placeholder="- Select path -" unique="unique">'
        +'          <option value=""></option>'
        +'      </select>'
        +'  </div>'
        +'  <div class="col-md-2">'
        +'      <input name="value_{i}" type="text" class="form-control" placeholder="Value">'
        +'  </div>'
        +'  <div class="col-md-1">'
        +'    <div class="remove-parameter">'
        +'      <a type="button" class="btn btn-xs btn-default btn-icon legitRipple btn-remove-parameter" rowOrder="{i}"><i class="icon-minus3"></i></a>'
        +'    </div>'
        +'  </div>'
        +'</div>';

    var html_danger = ''
        +'<div class="row tag-parameter-row" rowOrder="{i}">'
        +'  <div class="col-md-3">'
        +'    <div class="form-group">'
        +'      <input name="shortName_{i}" type="text" class="form-control border-bottom-danger text-danger" placeholder="Short name" required="required" unique="unique">'
        +'    </div>'
        +'  </div>'
        +'  <div class="col-md-6">'
        +'    <div class="form-group">'
        +'      <select name="path_{i}" class="form-control border-bottom-danger parameter-path" required="required" data-placeholder="- Select path -" unique="unique">'
        +'          <option value=""></option>'
        +'      </select>'
        +'    </div>'
        +'  </div>'
        +'  <div class="col-md-2">'
        +'    <div class="form-group">'
        +'      <input name="value_{i}" type="text" class="form-control border-bottom-danger text-danger" placeholder="Value">'
        +'      <input class="notExistParam" value="1" type="text" style="display: none">'
        +'    </div>'
        +'  </div>'
        +'  <div class="col-md-1">'
        +'    <div class="form-group remove-parameter">'
        +'      <a type="button" class="btn btn-xs btn-default btn-icon legitRipple btn-remove-parameter" rowOrder="{i}"><i class="icon-minus3"></i></a>'
        +'    </div>'
        +'  </div>'
        +'</div>';

    var html_new = ''
        +'<div class="row tag-parameter-row" rowOrder="{i}">'
        +'  <div class="col-md-3">'
        +'    <div class="form-group">'
        +'      <input name="shortName_{i}" type="text" class="form-control border-bottom-info text-info" placeholder="Short name" required="required" unique="unique">'
        +'    </div>'
        +'  </div>'
        +'  <div class="col-md-6">'
        +'    <div class="form-group">'
        +'      <select name="path_{i}" class="form-control parameter-path border-bottom-info" required="required" data-placeholder="- Select path -" unique="unique">'
        +'          <option value=""></option>'
        +'      </select>'
        +'    </div>'
        +'  </div>'
        +'  <div class="col-md-2">'
        +'    <div class="form-group">'
        +'      <input name="value_{i}" type="text" class="form-control border-bottom-info text-info" placeholder="Value">'
        +'    </div>'
        +'  </div>'
        +'  <div class="col-md-1">'
        +'    <div class="form-group remove-parameter">'
        +'      <a type="button" class="btn btn-xs btn-default btn-icon legitRipple btn-remove-parameter" rowOrder="{i}"><i class="icon-minus3"></i></a>'
        +'    </div>'
        +'  </div>'
        +'</div>';

    function generateSelectParentObjectParams(callback) {
        // Reset list option
        $("select.parameter-path-parent").html('<option value=""></option>');

        // Add new list option
        objectParams.split(",").forEach(function (e) {
            if (e !== "") {
                $("select.parameter-path-parent").append('<option value="' + e + '">' + e + '</option>');
            }
        });

        if(callback && typeof callback === "function"){
            callback();
        }
    }
    function generateSelectParams(parentObjectValue, index, callback) {
        var newParams = [];
        params.split(",").forEach(function (e) {
            if(e.substr(e.length - 1) !== '.' && e.search(parentObjectValue) >= 0) {
                var p = e.replace(parentObjectValue, "");
                if (p !== "") {
                    newParams.push(p);
                }
            }
        });

        newParams.forEach(function (e) {
            if (e !== "") {
                $('.tag-parameter-row[rowOrder='+index+']').find("select.parameter-path").append('<option value="' + e + '">' + e + '</option>');
            }
        });

        $('.parameter-path').select2();
        $('.tag-parameter-row').find('input').on('click', appendRow);
        $('.tag-parameter-row').find('select').on('change', appendRow);

        if(callback && typeof callback === "function"){
            callback();
        }
    }

    var appendRow = function() {
        var currentRow = $(this).parents('.tag-parameter-row').get(0);
        var nb = parseInt($(currentRow).attr('rowOrder'));
        var nextRow = $(currentRow).next();
        if(nextRow.length === 0) {
            nb+=1;
            $(currentRow).after(html.split("{i}").join(""+nb+""));
            generateSelectParams(parentObjectValue, nb, function () {
                // Add function remove row parameter
                $('.tag-parameter-row').find('.btn-remove-parameter').on('click', function () {
                    var index = parseInt($(this).attr('rowOrder'));
                    $('.tag-parameter-row[rowOrder='+index+']').html('');
                });
            });
        }
    };


    /**************************************************************
     * CREATE TAG
     */
    var validator = $(".form-create-tag").validate(ump.validator);
    var createTagDialog = $('#create-tag-dialog');

    // Reset form params in create tag
    function resetFormParams() {
        createTagDialog.find('.list-parameter-row').html(html.split("{i}").join("" + 1 + ""));
        parentObjectValue = $("select#parentObject").val();
        generateSelectParams(parentObjectValue, 1);
        validator.resetForm();
    }

    // Change select box tag type | show or hide input parent object
    createTagDialog.find("#tagType").change(function () {
        if (createTagDialog.find("#tagType").val() === "OBJECT") {
            createTagDialog.find(".formGroupParentObject").show();
            resetFormParams();
        } else {
            createTagDialog.find("#parentObject").val(null).change();
            createTagDialog.find(".formGroupParentObject").hide();
            resetFormParams();
        }
    });

    // Change parent object | generate new list params
    $("select#parentObject").change(function () {
        resetFormParams();
    });

    $('#create-tag-dialog-opener').click(function () {

        createTagDialog.modal('show');

        // Reset form tag
        createTagDialog.find("input").val(null);
        createTagDialog.find("select").val(null).change();
        createTagDialog.find(".formGroupParentObject").hide();

        generateSelectParentObjectParams(function () {
            resetFormParams();
        });
    });

    /*********************************************
     * UPDATE TAG
     */
    var validatorEdit = $(".form-edit-tag").validate(ump.validator);
    var editTagDialog = $('#edit-tag-dialog');
    // Reset form params
    function resetFormEditTagParams() {
        editTagDialog.find('.list-parameter-row').html(html.split("{i}").join("" + 1 + ""));
        parentObjectValue = $("select#parentObjectEdit").val();
        generateSelectParams(parentObjectValue, 1);
        validatorEdit.resetForm();
    }

    $('#edit-tag-dialog').on('hidden.bs.modal', function () {
        editTagDialog.find("#tagTypeEdit").unbind( "change" );
        editTagDialog.find("select#parentObjectEdit").unbind( "change" );
    });

    $('#create-tag-dialog').on('hidden.bs.modal', function () {
        editTagDialog.find("#tagTypeEdit").unbind( "change" );
        editTagDialog.find("select#parentObjectEdit").unbind( "change" );
    });

    $("#list_tags_table").delegate('tr', 'click', function () {

        editTagDialog.modal('show');

        // Reset form html
        var i = 1;
        editTagDialog.find("input").val(null);
        editTagDialog.find("select").val(null).change();
        editTagDialog.find('.list-parameter-row').html('');
        editTagDialog.find("#editTag").attr("type", 'submit');
        editTagDialog.find("#editTag").removeClass("disabled");
        validatorEdit.resetForm();

        // Get detail tag
        var tag_id = $(this).find('.tag_id').val();
        $.get(document.location.origin + "/data-models/tag/" + tag_id, function (data) {
            parentObjectValue = data.parentObject;

            // Get list params diff
            var listRemovedParamsById = [];
            var listNewParamsById = [];
            $.get(document.location.origin + "/data-models/" + $('#currentDeviceTypeVersionId').val() + "/get-list-diff-params", function (data2) {

                for (var index = 0; index < data2.REMOVED_PARAMETERS.length; index++) {
                    listRemovedParamsById[data2.REMOVED_PARAMETERS[index]] = data2.REMOVED_PARAMETERS[index];
                }
                for (index = 0; index < data2.NEW_PARAMETERS.length; index++) {
                    listNewParamsById[data2.NEW_PARAMETERS[index]] = data2.NEW_PARAMETERS[index];
                }

                // Set tag value
                editTagDialog.find("#tagIdEdit").val(data.id);
                editTagDialog.find("#tagNameEdit").val(data.name);
                editTagDialog.find("#tagTypeEdit").val(data.type).change();

                // Check type tag | Show input parent object
                if (data.type && data.type === "OBJECT") {
                    editTagDialog.find(".formGroupParentObject").show();
                } else {
                    editTagDialog.find(".formGroupParentObject").hide();
                }

                generateSelectParentObjectParams(function () {

                    editTagDialog.find("#tagTypeEdit").change(function () {
                        if (editTagDialog.find("#tagTypeEdit").val() === "OBJECT") {
                            editTagDialog.find("#parentObjectEdit").val(null).change();
                            editTagDialog.find(".formGroupParentObject").show();
                            resetFormEditTagParams();
                        } else {
                            editTagDialog.find("#parentObjectEdit").val(null).change();
                            editTagDialog.find(".formGroupParentObject").hide();
                            resetFormEditTagParams();
                        }
                    });

                    editTagDialog.find("#parentObjectEdit").val(parentObjectValue).change();

                    // Change parent object | generate new list params
                    $("select#parentObjectEdit").change(function () {
                        resetFormEditTagParams();
                    });

                    // Add list params
                    for (var key in data.parameters) {

                        if (listRemovedParamsById[data.parameters[key].path] && listRemovedParamsById[data.parameters[key].path] !== "") {
                            // Param removed
                            editTagDialog.find('.list-parameter-row').append(html_danger.split("{i}").join("" + i + ""));
                            editTagDialog.find("#editTag").attr("type", 'button');
                            editTagDialog.find("#editTag").addClass("disabled");

                        } else if (listNewParamsById[data.parameters[key].path] && listNewParamsById[data.parameters[key].path] !== "") {
                            // Params add new
                            editTagDialog.find('.list-parameter-row').append(html_new.split("{i}").join("" + i + ""));

                        } else {
                            // Default param
                            editTagDialog.find('.list-parameter-row').append(html.split("{i}").join("" + i + ""));
                        }

                        var newParams = [];
                        params.split(",").forEach(function (e) {
                            if(e.substr(e.length - 1) !== '.' && e.search(parentObjectValue) >= 0) {
                                var p = e.replace(parentObjectValue, "");
                                if (p !== "") {
                                    newParams.push(p);
                                }
                            }
                        });
                        newParams.forEach(function (e) {
                            if (e !== "") {
                                $('.tag-parameter-row[rowOrder='+i+']').find("select.parameter-path").append('<option value="' + e + '">' + e + '</option>');
                            }
                        });

                        // Set value form input
                        editTagDialog.find("input[name=shortName_" + i + "]").val(data.parameters[key].shortName);
                        editTagDialog.find("select[name=path_" + i + "]").val(data.parameters[key].path).change();
                        editTagDialog.find("input[name=value_" + i + "]").val(data.parameters[key].value);

                        i++;

                        $('.parameter-path').select2();
                    }

                    // Binding for new rows (auto suggest and appending row events)
                    $('.tag-parameter-row').find('input').on('click', appendRow);
                    $('.tag-parameter-row').find('select').on('change', appendRow);


                    // Add function remove row parameter
                    editTagDialog.find('.tag-parameter-row').find('.btn-remove-parameter').on('click', function () {

                        if(editTagDialog.find('.tag-parameter-row').find('.btn-remove-parameter').length > 1) {
                            var index = parseInt($(this).attr('rowOrder'));
                            editTagDialog.find('.tag-parameter-row[rowOrder=' + index + ']').remove();
                        }

                        if (typeof $(".notExistParam").val() === 'undefined' && editTagDialog.find("#editTag")) {
                            editTagDialog.find("#editTag").attr("type", 'submit');
                            editTagDialog.find("#editTag").removeClass("disabled");
                        }
                    });

                });

                $(".form-edit-tag").validate(ump.validator);
            });
        });
    });

    $('#confirm-delete-tag-assigned').dialog({
        autoOpen: false,
        modal: true,
        width: 500
    });

    // Default initialization
    $('.select').select2();
});

// VALIDATE FILE UPLOAD
function validateFileUpload(input, extensionStr) {
    if (input.type === "file") {
        var _validFileExtensions = extensionStr.split(',');
        var sFileName = input.value;
        if (sFileName.length > 0) {
            var blnValid = false;
            for (var j = 0; j < _validFileExtensions.length; j++) {
                var sCurExtension = _validFileExtensions[j];
                if (sFileName.substr(sFileName.length - sCurExtension.length, sCurExtension.length).toLowerCase() === sCurExtension.toLowerCase()) {
                    blnValid = true;
                    break;
                }
            }
            if (!blnValid) {
                $('#file-data-model-error-message').show();
                input.value = "";
                return false;
            }
        }
    }
    $('#file-data-model-error-message').hide();
    return true;
}

angular.module('UMPApp', []).controller('DataModelController', function ($scope, $http, $location) {
$('.select2').select2();
    // DECLARE VARIABLE
    var TAG_GROUP_OVERVIEW = 0;
    var TAG_GROUP_CONFIGURATION = 1;
    var TAG_GROUP_PROVISIONING = 2;

    $scope.isCreate = true;
    $scope.tag = {};
    $scope.listTagNoneAssign = [];
    $scope.listTagAssigned = [];
    $scope.listTagNoneAssignById = [];
    $scope.listInputType = ['select', 'checkbox', 'text', 'hidden', 'textarea', 'password','number'];
    var assignedGroup = null;
    var currentDeviceTypeVersionId = null;
    currentDeviceTypeVersionId = $('#currentDeviceTypeVersionId').val();
    $scope.listNewParameters = [];
    $scope.listRemovedParam = [];
    $scope.length_tagList = 0;
    $scope.length_removeList = 0;
    $scope.length_addList = 0;
    // INIT

    if(currentDeviceTypeVersionId) {
        getListTags();
    }
    $scope.tabActive = window.location.hash.replace('#/', '');
    

    // LIST EVENT FORM ASSIGN TAG
    $scope.cancel = function () {
        $scope.tag = {};
        $('#assign-tag-overview').modal('hide');
        $('#assign-tag-configuration').modal('hide');
        $('#assign-tag-provisioning').modal('hide');
        $('#confirm-delete-tag-assigned').dialog('close');
        $('#create-tag-dialog').modal('hide');
        $('#edit-tag-dialog').modal('hide');
    };

    $scope.selectTag = function (tag_id) {
        $scope.tag.parentObject = $scope.listTagNoneAssignById[tag_id] ? $scope.listTagNoneAssignById[tag_id].parentObject : null;
        $scope.tag.name = $scope.listTagNoneAssignById[tag_id] ? $scope.listTagNoneAssignById[tag_id].name : null;
        $scope.tag.type = $scope.listTagNoneAssignById[tag_id] ? $scope.listTagNoneAssignById[tag_id].type : null;
        $scope.tag.parameters = $scope.listTagNoneAssignById[tag_id] ? $scope.listTagNoneAssignById[tag_id].parameters : null;
    };

    $scope.submit = function (tag) {
        var url;
        var data;
        if($scope.isCreate) {
            url = document.location.origin + '/data-models/' + currentDeviceTypeVersionId + '/assign';
            data = $scope.listTagNoneAssignById[tag.rootTagId] ? $scope.listTagNoneAssignById[tag.rootTagId] : {};
            data.rootTagId = tag.rootTagId;
            data.assignedGroup = assignedGroup;
            data.parameters = tag.parameters;
        } else {
            url = document.location.origin + '/data-models/' + currentDeviceTypeVersionId + '/assign/update';
            data = tag;
        }

        $http({ method: 'POST', url: url, data: data}).then(function () {
            getListTags();
            $scope.cancel();
        });
    };


    // LIST EVENT LIST ASSIGNED TAGS
    $scope.deleteTagAssigned = function (tag_id) {
        $('#confirm-delete-tag-assigned').dialog('open');
        $scope.delete = function () {
            var url = document.location.origin + '/data-models/' + currentDeviceTypeVersionId + '/assign/' + tag_id;
            $http({ method: 'DELETE', url: url}).then(function () {
                getListTags();
                $scope.cancel();
            });
        };
    };
    
    $scope.editTagOverview = function (tag) {
        $scope.isCreate = false;
        $scope.tag = angular.copy(tag);
        $('#assign-tag-overview').modal('show');
    };
    $scope.editTagConfiguration = function (tag) {
        $scope.isCreate = false;
        $scope.tag = angular.copy(tag);
        $('#assign-tag-configuration').modal('show');
    };
    $scope.editTagProvisioning = function (tag) {
        $scope.isCreate = false;
        $scope.tag = angular.copy(tag);
        $('#assign-tag-provisioning').modal('show');
    };

    $scope.assignTagOverview = function () {
        getFormData("overview");
        $scope.tag = {};
        $scope.isCreate = true;
        assignedGroup = TAG_GROUP_OVERVIEW;
        $("#assign-tag-overview").find('select[name="tagId"]').val('');
        $("#assign-tag-overview").find('select[name="tagId"]').select2();
        $('#assign-tag-overview').modal('show');
    };
    $scope.assignTagConfiguration = function () {
        getFormData("configuration");
        $scope.tag = {};
        $scope.isCreate = true;
        assignedGroup = TAG_GROUP_CONFIGURATION;
        $("#assign-tag-configuration").find('select[name="tagId"]').val('');
        $("#assign-tag-configuration").find('select[name="tagId"]').select2();
        $('#assign-tag-configuration').modal('show');
    };
    $scope.assignTagProvisioning = function () {
        getFormData("provisioning");
        $scope.tag = {};
        $scope.tag.rootTagId = '';
        $scope.isCreate = true;
        assignedGroup = TAG_GROUP_PROVISIONING;
        $("#assign-tag-provisioning").find('select[name="tagId"]').val('');
        $("#assign-tag-provisioning").find('select[name="tagId"]').select2();
        $('#assign-tag-provisioning').modal('show');
    };

    // DECLARE FUNCTION
    function getListTags() {
        $http({
            method: 'GET',
            url: document.location.origin + '/data-models/' + currentDeviceTypeVersionId + '/assign-tag'
        }).then(function () {
            $scope.listTagAssigned = response.data;
        })
    }
    function getFormData(type) {

        $http({
            method: 'GET',
            url: document.location.origin + '/data-models/'+ currentDeviceTypeVersionId +'/list-tag-none-assign'
        }).then(function (response) {

            var listTagFull = [];
            angular.forEach(response.data, function (e) {
                listTagFull[e.id] = e;
            });

            var listTag = angular.copy(listTagFull);
            angular.forEach($scope.listTagAssigned[type], function (e) {
                if(listTag[e.rootTagId]) {
                    listTag[e.rootTagId] = null;
                }
            });

            $scope.listTagNoneAssign = [];
            angular.forEach(listTag, function (e) {
                if(e != null) {
                    $scope.listTagNoneAssign.push(e);
                }
            });
            $scope.listTagNoneAssignById = [];
            angular.forEach($scope.listTagNoneAssign, function (e) {
                $scope.listTagNoneAssignById[e.id] = e;
            });
        });
    }

    $scope.loadListTag = function () {
          $http({
                 method: 'GET',
                 url: document.location.origin + '/data-models/'+ currentDeviceTypeVersionId +'/list-new-parameters'
          }).then(function (response) {
                 $scope.listNewParameters = response.data;
                 $scope.length_addList = $scope.listNewParameters .length;
          });
          $http({
                 method: 'GET',
                 url: document.location.origin + '/data-models/'+ currentDeviceTypeVersionId +'/list-removed-param'
          }).then(function (response) {
                  $scope.listRemovedParam = response.data;
                  $scope.length_removeList =  $scope.listRemovedParam.length;
          });
          $http({
                 method: 'GET',
                 url: document.location.origin + '/data-models/'+ currentDeviceTypeVersionId +'/list-tag-none-assign'
          }).then(function (response) {
                 $scope.listTagNoneAssign = response.data;
                 $scope.length_tagList =  $scope.listTagNoneAssign.length;
                 if( $scope.listTagNoneAssign.length>0){
                       for (var i in  $scope.listTagNoneAssign) {
                           if(i.hasOwnProperty($scope.listTagNoneAssign)) {
                               for(var j in  $scope.listTagNoneAssign[i].parameters){
                                   if(j.hasOwnProperty($scope.listTagNoneAssign[i].parameters)) {
                                       for (var k in  $scope.listNewParameters) {
                                           if($scope.listNewParameters[k].localeCompare($scope.listTagNoneAssign[i].parameters[j].path) === 0){
                                               $scope.listTagNoneAssign[i].mess = "marked";
                                           }
                                       }
                                       for (var h in  $scope.listRemovedParam) {
                                           if($scope.listRemovedParam[h].localeCompare($scope.listTagNoneAssign[i].parameters[j].path) === 0){
                                               $scope.listTagNoneAssign[i].mess = "marked";
                                           }
                                       }
                                   }
                               }
                           }
                       }
                 }

           });

    };
    $scope.loadListTag();
        var totalDeviceInPage = $.trim($('#totalDeviceInPage').text());
        var totalDeviceBeChecked = 0;
        var manufacturerDataLast='';
        var _sortingField = $('#sortField').text();
        var _sortingType = $('#sortType').text();
        var _manufacturer = $('#manufacturerData').val();
        var _modelName = $('#modelNameData').val();
        var paginator_data = $('#data-data-model-paginator');
        if(_modelName == null){
            _modelName = "All"
        }
        if(_manufacturer == null){
            _manufacturer = "All"
        }

    $('.myCheckBox').each(function() {
        if ($(this).is(':checked')) {
            totalDeviceBeChecked += 1;
        }
    });
    $('.myCheckBox').change(function() {
        if ($(this).is(":checked")) {
            totalDeviceBeChecked = totalDeviceBeChecked + 1;
        } else {
            totalDeviceBeChecked = totalDeviceBeChecked - 1;
        }
        if (totalDeviceInPage === totalDeviceBeChecked) {
            $('#checkAll').prop('checked', true);
        } else {
            $('#checkAll').prop('checked', false);
        }
        if (totalDeviceBeChecked > 0) {
            showGroupButton(true);
        } else {
            showGroupButton(false)
        }
    });
    if (totalDeviceBeChecked > 0) {
        showGroupButton(true);
    } else {
        showGroupButton(false)
    }
    $('#checkAll').change(function() {
        if ($(this).is(":checked")) {
            checkAll();
        } else {
            unCheckAll();
        }

    });
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
            $('#confirm-delete-dialog-opener').removeClass("disabled");
        } else {
            $('#confirm-delete-dialog-opener').addClass("disabled");
        }

    }
    $("select#team").on("change", function() {
        var selectedD = $(this).val();
        if (selectedD === "1") {
        }
        if (selectedD === "2") {
        }
    });
        $('#confirm-delete-dialog').dialog({
            autoOpen: false,
            modal: true,
            width: '50%',
            height: 'auto'
        });
        $('#confirm-delete-now-dialog').click(function() {
            var hasDevice = false;
            var deleteIds = [];
            var deleteParams = [];
            $('.myCheckBox').each(function() {
                var id = $(this).attr('id');
                var name = $(this).attr('name');
                if ($(this).is(':checked')) {
                    deleteIds.push(id);
                    deleteParams.push(name);
                    hasDevice = true;
                }
            });
            if (hasDevice === false) {
                alert("No device being delete");
            } else {
                buttonLoading("confirm-delete-dialog-opener","start");
                $.ajax({
                    type: 'POST',
                    url: "/data-models/delete",
                    data: {
                        deleteIds : deleteIds + '@',
                        deleteParams : deleteParams + '@'
                    },
                    success: function(response) {
                            $('#confirm-delete-dialog').dialog('close');
                            buttonLoading("confirm-delete-dialog-opener","stop");
                            window.location.href = "/data-models";
                    },error: function (e) {
                    },
                    async: true
                });
            }
        });
    $('#confirm-delete-dialog-opener').click(function() {
        $('.ui-dialog-title').html('Do you want to delete these Data Models ?');
        $('#confirm-delete-dialog').dialog('open');
    });
    $('#confirm-delete-dialog-close').click(function() {
        $('#confirm-delete-dialog').dialog('close');
    });

    function searchDataModel(manufacturer, modelName) {
            var keySort = getParameterByName('sortField');
            var sortType = getParameterByName('sortType');
            var limitPageInput = parseInt($('.limit-page-input').val());
            if(isNaN(limitPageInput)){
                limitPageInput = '20';
            }
            var totalParam = "/data-models/search?indexPage=1"+"&limit="+limitPageInput;
            var tmp;

            if(keySort != null){
                totalParam += "&sortField=" + keySort + "&sortType=" + sortType;
            }

            if(manufacturer != 'All'){
                totalParam += "&manufacturer=" + manufacturer;
            }
            if(modelName != 'All'){
                totalParam += "&modelName=" + modelName;
            }

            window.location.href =totalParam;
     }

    $("#manufacturerData").change(function(){
        var modelNameVar = $("#modelNameData").val();
        console.log("modelNameVar : "+modelNameVar);
        var manufacturerDataVar = $(this).val();
        if(manufacturerDataLast != manufacturerDataVar){
            modelNameVar = "All";
        }

        searchDataModel(manufacturerDataVar,modelNameVar);
    });

    $("#modelNameData").change(function(){
        var modelNameVar = $(this).val();
        var manufacturerDataVar = $("#manufacturerData").val();
            searchDataModel(manufacturerDataVar,modelNameVar);
    });

    $(document).on('click', 'th[name=sortColumn]', function() {
        var sortType = $(this).attr('class');
        var keySort = $(this).children("span").attr('id');
        var indexPage = parseInt(paginator_data.attr('data-number'));
        var _manufacturer = $("#manufacturerData").val();
        var _modelName = $("#modelNameData").val();
        var limitPageInput = parseInt($('.limit-page-input').val());
        var totalParam = '';
        var totalPagesVar = parseInt(paginator_data.attr('data-totalPages'));


        var tmp;
        if (sortType.indexOf('sorting_asc') !== -1) {
            tmp = "sorting_desc";
        } else if (sortType.indexOf('sorting_desc') !== -1) {
            tmp = "sorting_asc"
        } else if (sortType.indexOf('sorting') !== -1) {
            tmp = "sorting_asc";
        } else tmp = "sorting_asc";
        totalParam = "/data-models/search?indexPage=" + indexPage + "&limit="+limitPageInput+"&sortField=" + keySort + "&sortType=" + tmp;
        if(_manufacturer != 'All' || _modelName != 'All'){
            if(_modelName == null){
                _modelName = "All";
            }
            totalParam += "&manufacturer=" + _manufacturer + "&modelName=" + _modelName
        }

        window.location.href = totalParam;
    });
    $(".row_deviceList").click(function() {
        window.location = $(this).data("href");
    });

    var lastPagesVar = parseInt(paginator_data.attr('data-lastPage'));
    var startPageVar = parseInt(paginator_data.attr('data-number'));
    $('.twbs-prev-next').twbsPagination({
        totalPages: lastPagesVar,
        visiblePages: 4,
        startPage: startPageVar,
        prev: '&#8672;',
        next: '&#8674;',
        first: '&#8676;',
        last: '&#8677;',
        onPageClick: function (event, page) {
            console.log("page : "+page);
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
            console.log("lastPagesVar : "+ lastPagesVar);
            console.log("goPageInput : "+ goPageInput);
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

        pagingDataModel(indexPage, limitPageInput);

    });

    function pagingDataModel(index, limit) {
        var _manufacturer = $("#manufacturerData").val();
        var _modelName = $("#modelNameData").val();
        var keySort = getParameterByName('sortField');
        var sortType = getParameterByName('sortType');
        var totalParam = '';
        totalParam = "/data-models/search?indexPage=" + index + "&limit=" + limit;

        if(keySort != null){
            totalParam += "&sortField=" + keySort + "&sortType=" + sortType;
        }

        if(_manufacturer != 'All' || _modelName != 'All'){
            if(_modelName == null){
                _modelName = "All";
            }
            totalParam += "&manufacturer=" + _manufacturer + "&modelName=" + _modelName
        }

        window.location.href = totalParam;
     }

     function getParameterByName(name) {
         url = window.location.href;
         name = name.replace(/[\[\]]/g, "\\$&");
         var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
             results = regex.exec(url);
         if (!results) return null;
         if (!results[2]) return '';
         return decodeURIComponent(results[2].replace(/\+/g, " "));
     }

});
