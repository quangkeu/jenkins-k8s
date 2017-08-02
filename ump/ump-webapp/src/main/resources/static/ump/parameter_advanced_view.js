function changeInput() {
    $('span#dataResponse').attr('data-is-change', true);
};

$(function(){

    var dataResponse = $('span#dataResponse');
    var deviceTypeVersionId = dataResponse.attr('data-deviceTypeVersionId');

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
    $('.search-parameter-advanced-view').val(searchStr ? searchStr : '');

    var listParameterObjectChecked = [];
    $(".tree-checkbox-advanced-view").fancytree({
        source: [],
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

    // GENERATE TREE OBJECT & LIST PARAMETER
    var html_row_parameter_access = '' +
        '<tr class="{resultSearch}">' +
        '   <td>{subPath}</td>' +
        '   <td>{inputParameter}</td>' +
        // '   <td><input type="text" class="form-control" name="{path}" value="{defaultValue}" onchange="changeInput()"></td>' +
        '   <td align="center">W</td>' +
        '</tr>';
    var html_row_parameter_no_access = '' +
        '<tr class="{resultSearch}">' +
        '   <td>{subPath}</td>' +
        '   <td>{defaultValue}</td>' +
        '   <td align="center">R</td>' +
        '</tr>';

    function initParameterCheckboxInput() {
        $('#parameter-advanced-view-form').find('.parameter-checkbox-input').unbind( "click" )
            .click(function () {
                var pathCheckbox = $(this).attr('data-parameterPath');

                if($(this).is(":checked")) {
                    $('#parameter-advanced-view-form').find('.parameter-checkbox-value[data-parameterPath="'+pathCheckbox+'"]').val('true');
                } else {
                    $('#parameter-advanced-view-form').find('.parameter-checkbox-value[data-parameterPath="'+pathCheckbox+'"]').val('false');
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
        $('#list-parameters-advanced-view').html('');
        var parameterObjArr = [];
        $.each(data, function (index, value) {
            var paramPath = value.path.replace(currentObjectStr+'.', "");
            if(paramPath.indexOf('.') < 0 && paramPath.length > 0) {
                value.subPath = value.path.substring(value.path.lastIndexOf(".") + 1, value.path.length);
                if($.inArray(parameterObjArr, value)) {
                    parameterObjArr.push(value);

                    var new_row = JSON.parse(value.access) ? html_row_parameter_access : html_row_parameter_no_access;
                    new_row = new_row.split("{path}").join(value.path);
                    new_row = new_row.split("{subPath}").join(paramPath);
                    new_row = new_row.split("{defaultValue}").join(value.defaultValue);
                    new_row = new_row.split("{inputParameter}").join(ump.generateParameterInput(value));
                    new_row = new_row.split("{resultSearch}").join(value.path.indexOf(searchStr) >= 0 ? 'search' : '');
                    $('#list-parameters-advanced-view').append(new_row);

                    // Init input mask if date time
                    if(value.dataType === 'dateTime') {
                        $('#list-parameters-advanced-view').find('input[name="path_'+value.path+'"]').inputmask("datetime")
                    }
                }
            }
        });

        // Set current object string
        $('#current-object-str').html(currentObjectStr);

        initParameterCheckboxInput();

        // Add redirect url
        $('#parameter-advanced-view-form').find('input[name=currentUrl]').val(location.pathname + location.search);
    }

    $.ajax({
        type : "GET",
        url : document.location.origin + '/data-models/'+deviceTypeVersionId+'/profile/get-list-parameters',
        success : function(data) {

            // Load array object string
            var objectStrArr = [];
            var allObjectStrArr = [];
            $.each(data, function (index, value) {
                if(value.path.indexOf(searchStr) >= 0 || typeof searchStr === 'undefined') {
                    var _objectStr = value.path.substring(0, value.path.lastIndexOf("."));
                    if(objectStrArr.indexOf(_objectStr) <= 0) {
                        objectStrArr.push(_objectStr);
                    }
                }

                _objectStr = value.path.substring(0, value.path.lastIndexOf("."));
                if(objectStrArr.indexOf(_objectStr) <= 0) {
                    allObjectStrArr.push(_objectStr);
                }
            });

            // Check no data result
            if(objectStrArr.length === 0) {
                $('#list-parameters-advanced-view').html('' +
                    '<tr>' +
                    '   <td colspan="5" align="center">No data result.</td>' +
                    '</tr>' +
                    '');
                generateTreeObject(allObjectStrArr, '', function (output) {
                    $('.tree-checkbox-advanced-view').fancytree("getTree").reload(output).done();
                });

            } else {
                var currentObjectStr = objectStrArr[0];
                if (objectStr && objectStr !== '') {
                    var currentObjectStr = objectStr;
                }
                ump.updateUrlQueryString('object', currentObjectStr);

                generateTreeObject(objectStrArr, currentObjectStr, function (output) {
                    $('.tree-checkbox-advanced-view').fancytree("getTree").reload(output).done();
                });

                generateListParameter(data, currentObjectStr);
            }


            // Change input search parameter
            $('.search-parameter-advanced-view').change(function () {
                var searchInputStr = $(this).val();
                var newParameterUrl = searchInputStr && searchInputStr !== '' ? '?search=' + searchInputStr : '';
                window.location.replace(document.location.origin + document.location.pathname + newParameterUrl);
            });

        }
    });


});

