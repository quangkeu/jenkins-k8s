
$(document).ready(function() {

    // DECLARE VARIABLE
    var parameters = [];
    var parametersByPath = [];
    var parametersSelected = [];
    var parametersSelectedValue = [];
    var parametersAutocomplete = [];
    var i = 0;

    // DECLARE HTML VARIABLE
    var form_customRPC = $('#form_customRPC');

        var set_parameter = "<cwmp:SetParameterValues xmls:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n  <ParameterList arrayType=\"cwmp:ParameterValueStruct{_i_}\">_parameter_\n  </ParameterList>\n </cwmp:SetParameterValues>";
        var parameter = "\n    <Name>_name_</Name>\n    <Value type=\"string\">_value_</Value>";
        var get_parameter = "<cwmp:GetParameterValues xmls:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n  <ParameterNames arrayType=\"xsd:string[_i_]\">_parameter_\n  </ParameterNames>\n </cwmp:GetParameterValues>";
        var get_parameter_attr = "<cwmp:GetParameterAttributes xmls:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n  <ParameterNames arrayType=\"xsd:string[_i_]\">_parameter_\n  </ParameterNames>\n </cwmp:GetParameterAttributes>";
        var parameterGet = "\n    <string>_name_</string>";
        var parameterList = "";

        $("#request").prop('disabled', true);

                $("#send_customRPC-close").click(function() {
                        var method_name = $("#method").val();
                        var deviceID = $("#send_customRPC").attr('name');
                        var totalParam = '';
                        totalParam = "/devices/device_customRPC/"+deviceID+"/"+method_name;
                        window.location.href = totalParam;
                });



                    $('.close').click(function() {
                        $(this).parent().hide();
                    });

    // ADVANCE SEARCH
    $('.select2').select2({
        allowClear: true
    });

    $("#method").change(function(){
        var method = $(this).val();
        var deviceID = $("#send_customRPC").attr('name');
        var totalParam = '';
        totalParam = "/devices/device_customRPC/"+deviceID+"/"+method;
        window.location.href = totalParam;
    });

        $("#send_customRPC").click(function() {
            var tableParameter = "";
            var deviceID = $(this).attr('name');
            var method_name = $("#method option:selected").val();
            var now = $('input[name=request]:checked', '#form_customRPC').val();
            if(method_name == "SetParameterValues" || method_name == "GetParameterValues" || method_name == "GetParameterAttributes") {
                    if(method_name == "SetParameterValues") tableParameter = $("#tableParameter").val();
                    else tableParameter = $("#tableParameterGet").val();
                    if(tableParameter == '' || tableParameter == null) {
                        var alert = $('#alert_danger_validate_input').attr('class');
                        $('#alert_danger_validate span.text-semibold').text('Parameter ' + alert);
                        $('#alert_danger_validate').show();
                    }

                    if(tableParameter !== '' && tableParameter !== null) {
                        $.ajax({
                            type: 'POST',
                            url: "/devices/device_customRPC/"+deviceID+"/"+method_name+"/"+now+"/send?parameter="+tableParameter,
                            success: function(AcsResponse) {
                            var successAlert = "";
                            var failedAlert = "";
                            $.each(AcsResponse.mapResult, function(id, result) {
                             if (result == 200) // success
                              {
                                successAlert = $('#alert_success_input').attr('class');
                              }
                             else { // failed
                                failedAlert = $('#alert_primary_input').attr('class');
                              }
                              });
                        if (successAlert !== "") {
                            $('#alert_success span.text-semibold').text(successAlert);
                            $('#alert_success').show();
                        }
                        if (failedAlert !== "") {
                            $('#alert_primary span.text-semibold').text(failedAlert);
                            $('#alert_primary').show();
                        }
                            },
                            error: function(data) {
                            },
                            async: true
                        });
                    }
            }



        });

    // FIRST LOAD PAGE
    firstLoadPage();

    function firstLoadPage() {
        initLibrary();
        getListParameters();
    }

    function initLibrary() {
        $('.add-parameter-input').autocomplete({
            source: []
        });
        parameterList = "";
        i = 0;
    }

    function getListParameters() {
        $.ajax({
            type: 'GET',
            url: location.origin + '/policy-configuration' + '/search-parameters',
            success: function (response) {
                parameters = response;
                parametersByPath = [];
                parametersAutocomplete = [];
                $.each(response, function (index, value) {
                    parametersByPath[value.path] = value;
                    parametersAutocomplete.push(value.path);
                });

                initAutocompleteParameter();
            }
        });
    }

    function initAutocompleteParameter() {
        var request = "";
        parameterList = "";
        var j = 0;
        var method_name = $("#method").val();
        parametersAutocomplete = [];
        $.each(parameters, function (index, value) {
            if(parametersSelected.indexOf(value.path) < 0) {
                parametersAutocomplete.push(value.path)
            }
        });
        if(method_name == "SetParameterValues") {
        $('#tableParameter').val(parametersSelectedValue);
        $.each(parametersSelectedValue, function (index, value) {
                parameterList = parameterList + parameter;
                parameterList = parameterList.replace(/\_name_/g, value.split("|")[0]);
                parameterList = parameterList.replace(/\_value_/g, value.split("|")[1]);
                j++;
        });
        if(parameterList !== '') {
                request = set_parameter.replace(/\_i_/g, i);
                request = request.replace(/\_parameter_/g, parameterList);
                $("#request").val(request);
        } else {
                request = set_parameter.replace(/\_i_/g, i);
                request = request.replace(/\_parameter_/g, parameterList);
                $("#request").val(request);
        }
        }
        if(method_name == "GetParameterValues" || method_name == "GetParameterAttributes") {
        $('#tableParameterGet').val(parametersSelected);
        $.each(parametersSelected, function (index, value) {
                parameterList = parameterList + parameterGet;
                parameterList = parameterList.replace(/\_name_/g, value);
                j++;
        });
        if(parameterList !== '') {
                if(method_name == "GetParameterValues") request = get_parameter.replace(/\_i_/g, i);
                else request = get_parameter_attr.replace(/\_i_/g, i);
                request = request.replace(/\_parameter_/g, parameterList);
                $("#request").val(request);
        } else {
                if(method_name == "GetParameterValues") request = get_parameter.replace(/\_i_/g, i);
                else  request = get_parameter_attr.replace(/\_i_/g, i);
                request = request.replace(/\_parameter_/g, parameterList);
                $("#request").val(request);
        }
        }
        form_customRPC.find(".add-parameter-input").autocomplete('destroy').autocomplete({
            source: parametersAutocomplete
        });

    }

    function initRemoveParameter() {
        form_customRPC.find('.remove-parameter-btn').unbind( "click" )
            .click(function () {
                i--;
                var pathRemove = $(this).attr('data-parameterPath');
                form_customRPC.find('.list-parameters').find('tr[data-parameterPath="'+pathRemove+'"]').remove();
                parametersSelected.splice(parametersSelected.indexOf(pathRemove), true);
                parametersSelectedValue.splice(parametersSelectedValue.indexOf(pathRemove), true);
                initAutocompleteParameter();
            });
    }

    // EVENT IN PAGE
    $('.add-parameter-btn').click(function () {
        var pathStr = $('.add-parameter-input').val();
        if(parametersSelected.indexOf(pathStr) < 0) {
            var parameterInsert = (typeof parametersByPath[pathStr] !== 'undefined') ? parametersByPath[pathStr] : {path: pathStr, defaultValue: ''};
            addParameter(parameterInsert);
        }
    });

    function addParameter(parameterInsert) {
        var method_name = $("#method").val();
        var value_range = $('#valueRange').val();
        if(parameterInsert !== null && parameterInsert.path !== '') {
            i++;
            $('.list-parameters-no-data').hide();

            // Add html
            var newRow = $('.list-parameters-template').html();
            newRow = newRow.split('{path}').join(parameterInsert.path);
            newRow = newRow.split('{inputParameter}').join(value_range);
            $('.list-parameters').append(newRow);

            $('.add-parameter-input').val('');
            $('#valueRange').val('');
            parametersSelected.push(parameterInsert.path);
            parametersSelectedValue.push(parameterInsert.path+'|'+value_range);
            initAutocompleteParameter();
            initRemoveParameter();
        }
    }

});