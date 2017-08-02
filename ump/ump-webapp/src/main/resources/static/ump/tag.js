$(function(){

    var dataResponse = $('span#dataResponse');

    // List profile
    $(".tree-default").fancytree({
        icon: false,
        click: function(event, data) {
            event.preventDefault();
            var start = data.node.title.indexOf('href="') + 'href="'.length;
            var end = data.node.title.indexOf('" title=');
            var nextUrl = data.node.title.substring(start, end);
            if(nextUrl && typeof nextUrl !== 'undefined') {
                if (dataResponse.attr('data-is-change') && confirm("Do you want to save current parameters?")) {
                    $('#parameter-form').submit();
                } else {
                    window.location.replace(document.location.origin + nextUrl);
                }
            }

        }
    });

    function getUrlVars() {
        var vars = {};
        var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi,
            function(m,key,value) {
                vars[key] = value;
            });
        return vars;
    }

    var objectStr = getUrlVars()['object'];
    var searchStr = getUrlVars()['search'];
    $('.search-parameter').val(searchStr ? searchStr : '');


    var listParameterObjectChecked = [];
    $(".tree-checkbox").fancytree({
        checkbox: true,
        selectMode: 3,
        loadChildren: function(event, ctx) {
            ctx.node.fixSelection3AfterClick();
        },
        source: [],
        select: function(event, data) {

            // Get a list of all selected nodes, and convert to a key array:
            listParameterObjectChecked = $.map(data.tree.getSelectedNodes(true), function(node){
                return node.key;
            });
            showMoveObjectButton(listParameterObjectChecked.length > 0)
        },
        activate: function(event, data) {
            //if (data.node.children == null) {
                if (dataResponse.attr('data-is-change') && confirm("Do you want to save current parameters?")) {
                    $('#parameter-form').submit();
                } else {
                    var newParameterUrl = '?object=' + data.node.key;
                    newParameterUrl += searchStr && searchStr !== '' ? '&search=' + searchStr : '';
                    window.location.replace(document.location.origin + document.location.pathname + newParameterUrl);
                }
            //}
        },

    });
    
    // CREATE PROFILE
    var form_create_tag_validator = $("#create-tag-form").validate(ump.validator);
    var create_tag_modal = $('#create-tag-modal');
    $(".tag-sidebar").find('#create-tag-modal-opener').click(function () {
        create_tag_modal.modal('show');
        $('.error-special-character').hide();
        create_tag_modal.find('input').val(null);
        form_create_tag_validator.resetForm();
    });


    /**
     * ADD PARAMETER TO PROFILE
     */
    var add_parameter_modal = $('#add-parameter-modal');
    var dataListPath = dataResponse.attr('data-list-path');
    var tagId = dataResponse.attr('data-tagId');
    var deviceTypeVersionId = dataResponse.attr('data-deviceTypeVersionId');
    var arrayPath = dataListPath ? dataListPath.split(",") : [];
    var arrayPathSelected = [];
    var html_row_param_selected = '' +
        '<tr data-path="{path}" class="path-selected-row">' +
        '   <td>{path}</td>' +
        '   <td><ul class="icons-list"><li> <a data-path="{path}" class="remove-path-selected"><i class="icon-trash"></i></a> </li></ul></td>' +
        '</tr>';

    // Open modal
    $(".add-parameter-btn-opener").click(function () {
        add_parameter_modal.modal('show');

        // Reset form
        add_parameter_modal.find('#list-param-selected').html('');
        add_parameter_modal.find('input[name=path]').val('');
        add_parameter_modal.find('input[name=path]').autocomplete({
            source: arrayPath
        });
    });

    // Add path selected to to table
    add_parameter_modal.find('input[name=path]').on('keypress', function (e) {
        if(e.which === 13){
            var path_selected = add_parameter_modal.find('input[name=path]').val();
            if($.inArray(path_selected, arrayPathSelected) < 0 && $.inArray(path_selected, arrayPath) >= 0) {

                // Add path selected to table
                $('#list-param-selected').append(html_row_param_selected.split("{path}").join(""+path_selected+""));
                arrayPathSelected.push(path_selected);

                // Reset input
                $("#add-parameter-modal").find('input[name=path]').val('');

                // Add function remove
                $('.remove-path-selected').unbind('click').click(function () {
                    var path_remove = $(this).attr('data-path');
                    $('#list-param-selected').find('tr[data-path="' + path_remove + '"]').remove();
                    arrayPathSelected.splice(arrayPathSelected.indexOf(path_remove), 1);
                })
            }
        }
    });

    // Save add parameter
    add_parameter_modal.find('#add-parameter-btn-save').click(function () {

        if (arrayPathSelected.length > 0) {
            var paths = {};
            $.each(arrayPathSelected, function (index, value) {
                paths['path_' + index] = value;
            });

            $.ajax({
                type : "POST",
                url : document.location.origin + '/data-models/'+deviceTypeVersionId+'/profile/'+tagId+'/add-parameter',
                data: {paths: paths},
                success : function(data) {
                    location.reload();
                }
            });
        } else {
            new PNotify({ text: 'Add some parameter before save.', addclass: 'bg-danger'});
        }



    });

    /**
     * MOVE PARAMETER
     */
    var move_parameter_modal = $('#move-parameter-modal');
    var listParameterChecked = [];
    $("#move-parameter-form").validate(ump.validator);
    function showMoveButton(isShow) {
        if(isShow) {
            $("#delete-parameter-btn").show();
            $("#move-parameter-btn").show();
            $("#save-parameter-btn").hide();
        } else {
            $("#delete-parameter-btn").hide();
            $("#move-parameter-btn").hide();
            $("#save-parameter-btn").show();
        }
    }
    showMoveButton(false);

    function checkAll() {
        listParameterChecked = [];
        $('.checkbox-row').each(function() {
            var path = $(this).attr('data-path');
            $('input[data-path="'+path+'"]').prop('checked', true);
            if(listParameterChecked.indexOf(path) < 0) {
                listParameterChecked.push(path);
            }
        });
        showMoveButton(true);
    }

    function unCheckAll() {
        listParameterChecked = [];
        $('.checkbox-row').each(function() {
            var path = $(this).attr('data-path');
            $('input[data-path="'+path+'"]').prop('checked', false);
        });
        showMoveButton(false);
    }

    $('input#checkbox-all').change(function() {
        if ($(this).is(":checked")) {
            checkAll();
        } else {
            unCheckAll();
        }
    });

    $('input.checkbox-row').change(function() {
        var path = $(this).attr('data-path');
        if ($(this).is(":checked")) {
            listParameterChecked.push(path);
        } else {
            listParameterChecked.splice(listParameterChecked.indexOf(path), 1);
        }
        showMoveButton(listParameterChecked.length > 0);
    });

    $("#move-parameter-btn").click(function () {
        move_parameter_modal.modal('show');
        $("#move-parameter-form").find('select').select2();
    });

    $('#cancel-move-parameter-btn').click(function () {
        move_parameter_modal.modal('hide');
        $("#move-parameter-form").find('select').val(null).change();
    });
    
    $('#save-move-parameter-btn').click(function () {
        var tagIdMove = $("#move-parameter-form").find('select').val();
        if(tagIdMove) {
            var paths = {};
            $.each(listParameterChecked, function (index, value) {
                paths['path_' + index] = value;
            });

            $.ajax({
                type : "POST",
                url : document.location.origin + '/data-models/'+deviceTypeVersionId+'/profile/'+tagId+'/move-parameter',
                data: {
                    tagIdMove: tagIdMove,
                    paths: paths
                },
                success : function(data) {
                    location.reload();
                }
            });
        }
    });

    $("#delete-parameter-btn").click(function () {
        if(confirm('Are you sure?')) {
            var paths = {};
            $.each(listParameterChecked, function (index, value) {
                paths['path_' + index] = value;
            });

            $.ajax({
                type : "POST",
                url : document.location.origin + '/data-models/'+deviceTypeVersionId+'/profile/'+tagId+'/delete-parameter',
                data: {
                    paths: paths
                },
                success : function() {
                    location.reload();
                }
            });
        }
    });


    /**
     * MOVE OBJECT PARAMETER
     */
    var move_parameter_object_modal = $('#move-parameter-object-modal');
    $("#move-parameter-object-form").validate(ump.validator);
    function showMoveObjectButton(isShow) {
        if(isShow) {
            $("#move-parameter-object-btn").show();
            $(".add-parameter-btn-opener").hide();
        } else {
            $("#move-parameter-object-btn").hide();
            $(".add-parameter-btn-opener").show();
        }
    }
    showMoveObjectButton(false);

    $("#move-parameter-object-btn").click(function () {
        move_parameter_object_modal.modal('show');
        $("#move-parameter-object-form").find('select').select2();
    });

    $('#cancel-move-parameter-object-btn').click(function () {
        move_parameter_object_modal.modal('hide');
        $("#move-parameter-object-form").find('select').val(null).change();
    });

    $('#save-move-parameter-object-btn').click(function () {
        var tagIdMove = $("#move-parameter-object-form").find('select').val();
        if(tagIdMove) {
            var paths = {};
            $.each(listParameterObjectChecked, function (index, value) {
                paths['path_' + index] = value;
            });

            $.ajax({
                type : "POST",
                url : document.location.origin + '/data-models/'+deviceTypeVersionId+'/profile/'+tagId+'/move-parameter-object',
                data: {
                    tagIdMove: tagIdMove,
                    paths: paths
                },
                success : function(data) {
                    location.reload();
                }
            });
        }
    });

    $('.remove-profile-opener').click(function () {
        var tagIdRemove = $(this).attr('data-tagId');
        if(confirm("Do you want to delete this profile?")) {
            $.ajax({
                type : "POST",
                url : document.location.origin + '/data-models/'+deviceTypeVersionId+'/profile/'+tagIdRemove+'/delete',
                success : function() {
                    location.reload();
                }
            });
        }
    });

    $('.synchronize-profile-opener').click(function () {
        var tagIdSynchronize = $(this).attr('data-tagId');
        if(confirm("Do you want to set this profile as automatically synchronize?")) {
            $.ajax({
                type : "POST",
                url : document.location.origin + '/data-models/profile/'+tagIdSynchronize+'/synchronize',
                success : function() {
                    location.reload();
                }
            });
        }
    });

    $('.unsynchronize-profile-opener').click(function () {
        var tagIdSynchronize = $(this).attr('data-tagId');
        if(confirm("Do you want to unsynchronize this profile?")) {
            $.ajax({
                type : "POST",
                url : document.location.origin + '/data-models/profile/'+tagIdSynchronize+'/synchronize',
                success : function() {
                    location.reload();
                }
            });
        }
    });

    // GENERATE TREE OBJECT & LIST PARAMETER
    var html_row_parameter_access = '' +
        '<tr class="{resultSearch}">' +
        '   <td align="center"><input type="checkbox" class="checkbox-row" data-path="{path}"></td>' +
        '   <td>{subPath}</td>' +
        '   <td>{inputParameter}</td>' +
        // '   <td><input type="text" class="form-control" name="{path}" value="{defaultValue}" onchange="changeInput()"></td>' +
        '   <td align="center">W</td>' +
        '</tr>';
    var html_row_parameter_no_access = '' +
        '<tr class="{resultSearch}">' +
        '   <td align="center"><input type="checkbox" class="checkbox-row" data-path="{path}"></td>' +
        '   <td>{subPath}</td>' +
        '   <td>{defaultValue}</td>' +
        '   <td align="center">R</td>' +
        '</tr>';

    function initParameterCheckboxInput() {
        $('#parameter-form').find('.parameter-checkbox-input').unbind( "click" )
            .click(function () {
                var pathCheckbox = $(this).attr('data-parameterPath');

                if($(this).is(":checked")) {
                    $('#parameter-form').find('.parameter-checkbox-value[data-parameterPath="'+pathCheckbox+'"]').val('true');
                } else {
                    $('#parameter-form').find('.parameter-checkbox-value[data-parameterPath="'+pathCheckbox+'"]').val('false');
                }

            });
    }
    function generateTreeObject(input, active ,callback) {
        output = [];
        for (var i = 0; i < input.length; i++) {
            var chain = input[i].split(".");
            var currentNode = output;
            var key = '';
            for (var j = 0; j < chain.length; j++) {
                var wantedNode = chain[j];
                key = key === '' ? key + wantedNode : key + '.' + wantedNode;
                wantedNode += '<span class="parentObjectResultSearch" style="display: none">'+key+'</span>';
                var lastNode = currentNode;
                for (var k = 0; k < currentNode.length; k++) {
                    if (currentNode[k].title == wantedNode) {
                        currentNode = currentNode[k].children;
                        break;
                    }
                }
                // If we couldn't find an item in this list of children
                // that has the right title, create one:
                if (lastNode == currentNode) {

                    // Set selected object
                    if(key === active) {
                        var newNode = currentNode[k] = { expanded: true, key: key, title: wantedNode, children: [], active: true,};
                    } else {
                        var newNode = currentNode[k] = { expanded: true, key: key, title: wantedNode, children: []};
                    }

                    currentNode = newNode.children;
                }
            }
        }

        if(callback && typeof callback == 'function') {
            callback(output);
        }
    }
    function generateListParameter(data, currentObjectStr) {
        $('#list-parameters').html('');
        var parameterObjArr = [];
        $.each(data, function (index, value) {
            var paramPath = value.path.replace(currentObjectStr+'.', "");
            if(paramPath.indexOf('.') < 0 && paramPath.length > 0) {
                value.subPath = value.path.substring(value.path.lastIndexOf(".") + 1, value.path.length);
                if($.inArray(parameterObjArr, value)) {
                    parameterObjArr.push(value);
                    var new_row = value.access === 'true' ? html_row_parameter_access : html_row_parameter_no_access;
                    new_row = new_row.split("{path}").join(value.path);
                    new_row = new_row.split("{subPath}").join(paramPath);
                    new_row = new_row.split("{defaultValue}").join(value.defaultValue);
                    new_row = new_row.split("{resultSearch}").join(value.path.indexOf(searchStr) >= 0 ? 'search' : '');
                    new_row = new_row.split('{inputParameter}').join(ump.generateParameterInput(value));
                    $('#list-parameters').append(new_row);

                    // Init input mask if date time
                    if(value.dataType === 'dateTime') {
                        $('#list-parameters').find('input[name="path_'+value.path+'"]').inputmask("datetime")
                    }
                }
            }
        });
        
        $('#list-parameters').find('.inputParameter').unbind('change').change(function () {
            $('span#dataResponse').attr('data-is-change', true);
        });
        $('#list-parameters').find('.parameter-checkbox-input').unbind('change').change(function () {
            $('span#dataResponse').attr('data-is-change', true);
        });

        // Init function change checkbox
        $('input.checkbox-row').change(function() {
            var path = $(this).attr('data-path');
            if ($(this).is(":checked")) {
                listParameterChecked.push(path);
            } else {
                listParameterChecked.splice(listParameterChecked.indexOf(path), 1);
            }
            showMoveButton(listParameterChecked.length > 0);
        });


        // Set current object string
        $('#current-object-str').html(currentObjectStr);

        initParameterCheckboxInput();
    }

    $.ajax({
        type: "GET",
        url: document.location.origin + '/data-models/profile/'+tagId+'/get-list-parent-objects',
        data: {
            parentObject: searchStr
        },
        success: function (response) {
            var _activeObject = ump.getUrlQueryValue('object');
            if (_activeObject === null) {
                _activeObject = response.parentObjects[0];
                ump.updateUrlQueryString('object', _activeObject);
            }

            generateTreeObject(response.parentObjects, _activeObject, function (output) {
                $('.tree-checkbox').fancytree("getTree").reload(output).done();

                // Highlight result search
                $('.tree-checkbox').find('.parentObjectResultSearch').each(function () {
                    if (response.parentObjectResultSearch.indexOf($(this).text()) >= 0) {
                        $(this).parent('.fancytree-title').css('background-color', '#F6F792');
                    }
                });
                // Remove highlight if active
                $('.tree-checkbox').find('.fancytree-active').find('.fancytree-title').removeAttr('style');

                // Get list parameter
                $.ajax({
                    type : "GET",
                    url : document.location.origin + '/data-models/profile/'+tagId+'/get-list-parameters',
                    success : function(data) {
                        // Check no data result
                        if(data.length === 0) {
                            $('#list-parameters').html('' +
                                '<tr>' +
                                '   <td colspan="5" align="center">No data result.</td>' +
                                '</tr>' +
                                '');
                        } else {
                            generateListParameter(data, _activeObject);
                        }

                        // Change input search parameter
                        $('.search-parameter').change(function () {
                            var searchInputStr = $(this).val();
                            var newParameterUrl = searchInputStr && searchInputStr !== '' ? '?search=' + searchInputStr : '';
                            window.location.replace(document.location.origin + document.location.pathname + newParameterUrl);
                        });

                        // Add redirect url
                        $('#parameter-form').find('input[name=currentUrl]').val(location.pathname + location.search);
                    }
                });

                // Change input search parameter
                $('.search-parameter').change(function () {
                    var searchInputStr = $(this).val();
                    var newParameterUrl = searchInputStr && searchInputStr !== '' ? '?search=' + searchInputStr : '';
                    window.location.replace(document.location.origin + document.location.pathname + newParameterUrl);
                });

                // Add redirect url
                $('#parameter-form').find('input[name=currentUrl]').val(location.pathname + location.search);
            });

        }
    });

    // Validate special character
    var alNumRegex = /^([a-zA-Z0-9]+)$/; //only letters and numbers
    $('.error-special-character').hide();
    $('#create-tag-form').find('input[name=name]').change(function () {
        if(alNumRegex.test($(this).val())) {
            $('.error-special-character').hide();
            $('#create-tag-form').attr('onsubmit', 'return true');
        } else {
            $('.error-special-character').show();
            $('#create-tag-form').attr('onsubmit', 'return false');
        }
    });

});