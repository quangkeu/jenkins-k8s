$(function () {

    //<editor-fold desc="Declare variable">
    var dataResponseHtml = $('.data-response');
    var treeObjectHtml = $('.treeObject');
    var formParameterHtml = $('.formParameter');
    var listParametersHtml = $('.listParameters');
    var labelCurrentParentObjectPathHtml = $('.labelCurrentParentObjectPath');
    var inputSearchParametersHtml = $('.inputSearchParameters');
    var btnAddObjectHtml = $('.btnAddObject');
    var btnDeleteObjectHtml = $('.btnDeleteObject');
    var btnRefreshObjectHtml = $('.btnRefreshObject');
    var btnSaveObjectHtml = $('.btnSaveObject');
    var modalAddObjectHtml = $('.modalAddObject');
    var listParametersModalHtml = $('.listParametersModal');

    var currentPathSearch = '';
    var currentParentObjectPath = '';
    var currentDeviceId = dataResponseHtml.attr('data-currentDeviceId');
    var currentDeviceTypeVersionId = dataResponseHtml.attr('data-currentDeviceTypeVersionId');

    var rowParameterNoDataHtml = '<tr class="text-center">' + '<td colspan="3">No result was found!</td>' + '</tr>';
    var rowParameterAccessHtml = '<tr class="{resultSearch}">' + '<td>{subPath}</td>' + '<td>{inputParameter}</td>' + '<td align="center">W</td>' + '</tr>';
    var rowParameterAccessModalHtml = '<tr class="{resultSearch}">' + '<td>{subPath}</td>' + '<td>{inputParameter}</td>' + '</tr>';
    var rowParameterNoAccessHtml = '<tr class="{resultSearch}">' + '<td>{subPath}</td>' + '<td>{defaultValue}</td>' + '<td align="center">R</td>' + '</tr>';
    //</editor-fold>

    //<editor-fold desc="First load">
    firstLoad();
    function firstLoad() {

        // Tree object
        treeObjectHtml.fancytree({
            checkbox: false,
            source: [],
            activate: function (event, data) {

                // Update current parent object
                currentParentObjectPath = data.node.key + '.';
                labelCurrentParentObjectPathHtml.text(currentParentObjectPath);

                // Update list parameters
                updateListParameters();
                updateGroupBtn();
                ump.updateUrlQueryString('object', currentParentObjectPath);

            }
        });

        currentPathSearch = ump.getUrlQueryValue('search');
        currentPathSearch = currentPathSearch != null ? currentPathSearch : '';
        inputSearchParametersHtml.val(currentPathSearch);

        // Get list parent object
        updateListParentObjects();

    }
    //</editor-fold>

    //<editor-fold desc="Event in page">

    // Search parameter
    inputSearchParametersHtml.change(function () {
        currentPathSearch = $(this).val();
        currentParentObjectPath = '';
        updateListParentObjects();
        ump.updateUrlQueryString('object', currentParentObjectPath);
        ump.updateUrlQueryString('search', currentPathSearch);
    });

    // Save change object
    btnSaveObjectHtml.click(function () {
        var listPathSave = [];
        formParameterHtml.find('input').each(function () {
            var inputName = $(this).attr('name');
            if(inputName.indexOf('path_') === 0) {
                listPathSave.push(inputName.substring('path_'.length, inputName.length));
            }
        });

        if (listPathSave.length > 0) {
            var messageSave = 'Do you want to save this change? \n' + listPathSave.join('\n');
            if(confirm(messageSave)) {
                progressBtn(btnSaveObjectHtml);
                formParameterHtml.append('<input type="hidden" name="parentObject" value="'+currentParentObjectPath+'">');
                formParameterHtml.append('<input type="hidden" name="currentUrl" value="'+ location.pathname + location.search+'">');
                formParameterHtml.unbind('submit').submit().find('[type=submit]').trigger('click');
            } else {
                return false;
            }
        } else {
            return false;
        }
    });

    // Refresh object
    btnRefreshObjectHtml.click(function () {
        if(confirm('Do you want to refresh object?')) {
            progressBtn(btnRefreshObjectHtml);
            $.ajax({
                type: "POST",
                url: document.location.origin + '/devices/device_advanced_view/'+currentDeviceId+'/post-refresh-object',
                data: {
                    parentObject: currentParentObjectPath
                },
                success: function (response) {
                    location.reload();
                }
            })
        }
    });

    // Delete object
    btnDeleteObjectHtml.click(function () {
        if (confirm('Do you want to delete ' + currentParentObjectPath + '?')) {
            progressBtn(btnDeleteObjectHtml);
            $.ajax({
                type: "POST",
                url: document.location.origin + '/devices/device_advanced_view/' + currentDeviceId + '/post-delete-object',
                data: {
                    parentObject: currentParentObjectPath,
                    deviceTypeVersionId: currentDeviceTypeVersionId
                },
                success: function (response) {
                    location.reload();
                }
            })
        }
    });

    //</editor-fold>

    //<editor-fold desc="Private function">

    function progressBtn(_btn){
        _btn.attr('disabled', true);
        _btn.html('<i class="fa fa-spinner fa-spin"></i>');
    }

    function hideGroupBtn() {
        btnAddObjectHtml.hide();
        btnDeleteObjectHtml.hide();
        btnRefreshObjectHtml.hide();
        btnSaveObjectHtml.hide();
    }

    function updateGroupBtn() {
        $.ajax({
            type: "GET",
            url: document.location.origin + '/devices/device_advanced_view/get-status-button',
            data: {
                deviceTypeVersionId: currentDeviceTypeVersionId,
                parentObject: currentParentObjectPath
            },
            success: function (response) {
                btnAddObjectHtml.show();
                btnDeleteObjectHtml.show();
                btnRefreshObjectHtml.show();
                btnSaveObjectHtml.show();
                btnAddObjectHtml.attr('disabled', !response.isAdd);
                btnDeleteObjectHtml.attr('disabled', !response.isDelete);
            }
        })
    }

    function generateTreeParentObject(input, resultSearch) {

        labelCurrentParentObjectPathHtml.text(currentParentObjectPath);

        // Reset data
        var active = currentParentObjectPath != null && currentParentObjectPath.length > 0 ? currentParentObjectPath.substring(0, currentParentObjectPath.length - 1) : '';
        var output = [];

        // Generate json tree object
        for (var i = 0; i < input.length; i++) {
            var chain = input[i].substring(0, input[i].length-1).split(".");
            var currentNode = output;
            var key = '';
            for (var j = 0; j < chain.length; j++) {
                var wantedNode = chain[j];
                key = key === '' ? key + wantedNode : key + '.' + wantedNode;
                var lastNode = currentNode;
                for (var k = 0; k < currentNode.length; k++) {
                    if (currentNode[k].title == wantedNode) {
                        currentNode = currentNode[k].children;
                        break;
                    }
                }

                if (lastNode == currentNode) {
                    if (key === active) {
                        var newNode;
                        if (resultSearch.indexOf(key + '.') >= 0) {
                            newNode = currentNode[k] = { expanded: true, key: key, title: wantedNode, children: [], active: true, 'selected': true};
                        } else {
                            newNode = currentNode[k] = { expanded: true, key: key, title: wantedNode, children: [], active: true};
                        }
                    } else {
                        if (resultSearch.indexOf(key + '.') >= 0) {

                            newNode = currentNode[k] = {expanded: true, key: key, title: wantedNode, children: [], 'selected': true};
                        } else {
                            newNode = currentNode[k] = {expanded: true, key: key, title: wantedNode, children: []};
                        }

                    }
                    currentNode = newNode.children;
                }
            }
        }

        // Reload tree
        treeObjectHtml.fancytree("getTree").reload(output).done();

        treeObjectHtml.find('.fancytree-title').each(function () {
            $(this).attr('title', $(this).text());
        });
    }

    function generateTableParameters(listParameters) {
        listParametersHtml.html('');
        $.each(listParameters, function (index, value) {

            var _parameterPath = value.path.replace(currentParentObjectPath, '');
            if (_parameterPath.indexOf('.') < 0 && _parameterPath.length > 0) {

                // Get value changed
                var changedValue = formParameterHtml.find('input[name="path_' + value.path + '"]').val();
                value.value = typeof changedValue !== 'changedValue' ? changedValue : value.value;

                // Append new row to table parameter
                var _rowHtml = '';
                if (value.access === 'true') {
                    _rowHtml = rowParameterAccessHtml;
                    _rowHtml = _rowHtml.split('{inputParameter}').join(ump.generateParameterInput(value));
                } else {
                    _rowHtml = rowParameterNoAccessHtml;
                    _rowHtml = _rowHtml.split("{defaultValue}").join(value.defaultValue);
                }
                if(currentPathSearch != '' && value.path.indexOf(currentPathSearch) >= 0) {
                    // High line result search
                    _rowHtml = _rowHtml.split("{resultSearch}").join('search');
                }
                _rowHtml = _rowHtml.split("{subPath}").join(_parameterPath);
                listParametersHtml.append(_rowHtml);

                // Init input mask if date time
                if(value.dataType === 'dateTime') {
                    listParametersHtml.find('input[name="path_'+value.path+'"]').inputmask("datetime")
                }

                // Event change value input text parameter
                listParametersHtml.find('input[name="path_'+value.path+'"]').unbind('change').change(function () {
                    updateFormParameterChanged(value.path, $(this).val(), value.dataType);
                });

                // Event change value input checkbox parameter
                listParametersHtml.find('.parameter-checkbox-input[data-parameterPath="' + value.path + '"]').unbind("click").click(function () {
                    var pathCheckbox = $(this).attr('data-parameterPath');
                    if ($(this).is(":checked")) {
                        listParametersHtml.find('.parameter-checkbox-value[data-parameterPath="' + pathCheckbox + '"]').val('true').trigger('change');
                    } else {
                        listParametersHtml.find('.parameter-checkbox-value[data-parameterPath="' + pathCheckbox + '"]').val('false').trigger('change');
                    }
                });

            }
        });

        if(listParameters.length == 0) {
            listParametersHtml.html(rowParameterNoDataHtml);
        }

    }

    function generateTableParametersModal(listParameters) {
        listParametersModalHtml.html('');
        listParametersModalHtml.append('<input type="hidden" name="objectPath" value="' + getObjectTr069(currentParentObjectPath) + '" >');
        listParametersModalHtml.append('<input type="hidden" name="currentUrlAddObject" value="' + location.pathname + location.search + '">');
        $.each(listParameters, function (index, value) {

            var _parameterPath = value.path.replace(currentParentObjectPath, '');
            if (_parameterPath.indexOf('.') < 0 && _parameterPath.length > 0) {

                // Append new row to table parameter
                var _rowHtml = '';
                if (value.access === 'true') {
                    _rowHtml = rowParameterAccessModalHtml;
                    _rowHtml = _rowHtml.split('{inputParameter}').join(ump.generateParameterInput(value));
                    _rowHtml = _rowHtml.split("{subPath}").join(_parameterPath);
                    listParametersModalHtml.append(_rowHtml);

                    // Init input mask if date time
                    if(value.dataType === 'dateTime') {
                        listParametersModalHtml.find('input[name="path_'+value.path+'"]').inputmask("datetime")
                    }

                    // Event change value input checkbox parameter
                    listParametersModalHtml.find('.parameter-checkbox-input[data-parameterPath="' + value.path + '"]').unbind("click").click(function () {
                        var pathCheckbox = $(this).attr('data-parameterPath');
                        if ($(this).is(":checked")) {
                            listParametersModalHtml.find('.parameter-checkbox-value[data-parameterPath="' + pathCheckbox + '"]').val('true');
                        } else {
                            listParametersModalHtml.find('.parameter-checkbox-value[data-parameterPath="' + pathCheckbox + '"]').val('false');
                        }
                    });
                }
            }
        });

        if(listParametersModalHtml.length == 0) {
            listParametersModalHtml.html(rowParameterNoDataHtml);
        }

    }

    function updateFormParameterChanged(parameterPath, parameterValue, parameterDataType) {
        formParameterHtml.find('input[name="path_' + parameterPath + '"]').remove();
        formParameterHtml.find('input[name="dataType_' + parameterPath + '"]').remove();
        formParameterHtml.append('<input name="path_' + parameterPath + '" value="' + parameterValue + '" >');
        formParameterHtml.append('<input name="dataType_' + parameterPath + '" value="' + parameterDataType + '" >')
    }

    function updateListParameters() {
        listParametersHtml.html('');
        $.ajax({
            type: "GET",
            url: document.location.origin + '/devices/device_advanced_view/' + currentDeviceId + '/get-list-parameters',
            data: {
                parentObject: currentParentObjectPath,
                deviceTypeVersionId: currentDeviceTypeVersionId
            },
            success: function (response) {
                // Update table parameters
                generateTableParameters(response);

                // Add object
                btnAddObjectHtml.unbind('click').click(function () {
                    modalAddObjectHtml.modal('show');
                    modalAddObjectHtml.find('.formAddObject').attr('action', '/devices/device_advanced_view/'+currentDeviceId+'/add-object');
                    modalAddObjectHtml.find('.labelParentObject').html(getObjectTr069(currentParentObjectPath));
                    generateTableParametersModal(response);
                });
            }
        });
    }

    function updateListParentObjects() {
        listParametersHtml.html('');
        hideGroupBtn();
        $.ajax({
            type: "GET",
            url: document.location.origin + '/devices/device_advanced_view/' + currentDeviceId + '/get-list-parent-objects',
            data: {
                parentObject: currentPathSearch,
                deviceTypeVersionId: currentDeviceTypeVersionId
            },
            success: function (response) {

                var _listParentObjects = response.parentObjects && response.parentObjects.length > 0 ? response.parentObjects : [];
                var _listParentObjectResultSearch = response.parentObjectResultSearch && response.parentObjectResultSearch.length > 0 ? response.parentObjectResultSearch : [];


                currentParentObjectPath = ump.getUrlQueryValue('object');
                currentParentObjectPath = currentParentObjectPath !== null && currentParentObjectPath !== '' ? currentParentObjectPath : _listParentObjects[0];
                updateListParameters();
                generateTreeParentObject(_listParentObjects, _listParentObjectResultSearch);
                updateGroupBtn();
                ump.updateUrlQueryString('object', currentParentObjectPath);
            }
        })
    }

    function getObjectTr069(currentParentObjectPath) {
        var result = '';
        var pathArr = currentParentObjectPath.split('.');
        pathArr.splice(pathArr.length-2, 1);
        result = pathArr.join('.');

        return result;
    }
    //</editor-fold>

    $('a').click(function (e) {
        e.preventDefault();
        var currentUrl = location.pathname;
        var nextUrl = $(this).attr('href');
        if (currentUrl && nextUrl && currentUrl !== nextUrl) {
            var listPathSave = [];
            formParameterHtml.find('input').each(function () {
                var inputName = $(this).attr('name');
                if(inputName.indexOf('path_') === 0) {
                    listPathSave.push(inputName.substring('path_'.length, inputName.length));
                }
            });

            if (listPathSave.length > 0) {
                var messageSave = 'Do you want to save this change? \n' + listPathSave.join('\n');
                if(confirm(messageSave)) {
                    progressBtn(btnSaveObjectHtml);
                    formParameterHtml.append('<input type="hidden" name="parentObject" value="'+currentParentObjectPath+'">');
                    formParameterHtml.append('<input type="hidden" name="currentUrl" value="'+ location.pathname + location.search+'">');
                    formParameterHtml.unbind('submit').submit().find('[type=submit]').trigger('click');
                } else {
                    location.replace(location.origin + nextUrl);
                }
            } else {
                location.replace(location.origin + nextUrl);
            }
        } else {
            location.replace(location.origin + nextUrl);
        }
    })

});