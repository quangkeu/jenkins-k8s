
$(document).ready(function() {
        var addObject = "<cwmp:AddObjetc xmls:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n  <ObjectName>\n    xxxx\n  </ObjectName>\n  <ParameterKey></ParameterKey>\n</cwmp:AddObject>";
        var deleteObject = "<cwmp:DeleteObjetc xmls:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n  <ObjectName>\n    xxxx\n  </ObjectName>\n  <ParameterKey></ParameterKey>\n</cwmp:DeleteObject>";
        var factoryReset = "<cwmp:FactoryReset xmls:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n  <CommandKey>xxxx</CommandKey>\n </cwmp:FactoryReset>";
        var reboot = "<cwmp:Reboot xmls:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n  <CommandKey>xxxx</CommandKey>\n </cwmp:Reboot>";
        var getParameterNames = "<cwmp:GetParameterNames xmls:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n  <ParameterPath>\n    xxxx\n  </ParameterPath>\n  <NextLevel>yyyy</NextLevel>\n</cwmp:GetParameterNames>";
        var upload = "<cwmp:Upload xmls:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n  <CommandKey>xxxx</CommandKey>\n  <FileType>yyyy</FileType>\n  <URL>url</URL>\n  <Username>username</Username>\n  <Password>password</Password>\n  <DelaySeconds>zzzz</DelaySeconds>\n </cwmp:Upload>";

        // DECLARE VARIABLE
        var parameters = [];
        var parametersByPath = [];
        var parametersSelected = [];
        var parametersSelectedValue = [];
        var parametersAutocomplete = [];

        var form_rpc = $('.form-validate-jquery');

        $("#request").prop('disabled', true);
        $("#request2").prop('disabled', true);

        $("#object").on("propertychange change keyup paste input", function(){
            var request = "";
           var method_name = $("#method").val();
           var object =  $(this).val();
           if(method_name == "addObject") request = addObject.replace(/\xxxx/g, object);
           if(method_name == "deleteObject") request = deleteObject.replace(/\xxxx/g, object);
           $("#request").val(request);
           if(object.length>0 && object.charAt(object.length - 1) != ".") $("#object").val(object + '.');
        });

        $("#parameterPath").on("propertychange change keyup paste input", function(){
            var request = "";
             var method_name = $("#method").val();
             if(method_name == "GetParameterNames"){
                var object =  $(this).val();
                var level = $("#level").val();
                request = getParameterNames.replace(/\xxxx/g, object);
                request = request.replace(/\yyyy/g, level);
                $("#request").val(request);
             }
        });

            $("#level").change(function(){
                var request = "";
                var method_name = $("#method").val();
                if(method_name == "GetParameterNames"){
                    var object =  $("#parameterPath").val();
                    var level = $("#level").val();
                    request = getParameterNames.replace(/\xxxx/g, object);
                    request = request.replace(/\yyyy/g, level);
                    $("#request").val(request);
                }
            });

        $("#commandKey").on("propertychange change keyup paste input", function(){
           var request = "";
           var method_name = $("#method").val();
           var object =  $(this).val();
           if(method_name == "FactoryReset") request = factoryReset.replace(/\xxxx/g, object);
           if(method_name == "Reboot") request = reboot.replace(/\xxxx/g, object);
           if(method_name == "Upload") {
                request = upload.replace(/\xxxx/g, object);
                request = request.replace(/\yyyy/g, $("#fileType").val());
                request = request.replace(/\url/g, $("#url").val());
                request = request.replace(/\username/g, $("#username").val());
                request = request.replace(/\password/g, $("#password").val());
                request = request.replace(/\zzzz/g, $("#delay").val());
           }
           $("#request").val(request);
        });

        $("#url").on("propertychange change keyup paste input", function(){
           var request = "";
           var method_name = $("#method").val();
           var url =  $(this).val();
           if(method_name == "Upload") {
                request = upload.replace(/\xxxx/g, $("#commandKey").val());
                request = request.replace(/\yyyy/g, $("#fileType").val());
                request = request.replace(/\url/g, url);
                request = request.replace(/\username/g, $("#username").val());
                request = request.replace(/\password/g, $("#password").val());
                request = request.replace(/\zzzz/g, $("#delay").val());
           }
           $("#request").val(request);
        });

        $("#username").on("propertychange change keyup paste input", function(){
           var request = "";
           var method_name = $("#method").val();
           var username =  $(this).val();
           if(method_name == "Upload") {
                request = upload.replace(/\xxxx/g, $("#commandKey").val());
                request = request.replace(/\yyyy/g, $("#fileType").val());
                request = request.replace(/\url/g, $("#url").val());
                request = request.replace(/\username/g, username);
                request = request.replace(/\password/g, $("#password").val());
                request = request.replace(/\zzzz/g, $("#delay").val());
           }
           $("#request").val(request);
        });

        $("#password").on("propertychange change keyup paste input", function(){
           var request = "";
           var method_name = $("#method").val();
           var password =  $(this).val();
           if(method_name == "Upload") {
                request = upload.replace(/\xxxx/g, $("#commandKey").val());
                request = request.replace(/\yyyy/g, $("#fileType").val());
                request = request.replace(/\url/g, $("#url").val());
                request = request.replace(/\username/g, $("#username").val());
                request = request.replace(/\password/g, password);
                request = request.replace(/\zzzz/g, $("#delay").val());
           }
           $("#request").val(request);
        });

        $("#delay").on("propertychange change keyup paste input", function(){
           var request = "";
           var method_name = $("#method").val();
           var delay =  $(this).val();
           if(method_name == "Upload") {
                request = upload.replace(/\xxxx/g, $("#commandKey").val());
                request = request.replace(/\yyyy/g, $("#fileType").val());
                request = request.replace(/\url/g, $("#url").val());
                request = request.replace(/\username/g, $("#username").val());
                request = request.replace(/\password/g, $("#password").val());
                request = request.replace(/\zzzz/g, delay);
           }
           $("#request").val(request);
        });

            $("#fileType").change(function(){
                var request = "";
                var method_name = $("#method").val();
                var fileType =  $(this).val();
                if(method_name == "Upload") {
                request = upload.replace(/\xxxx/g, $("#commandKey").val());
                request = request.replace(/\yyyy/g, fileType);
                request = request.replace(/\url/g, $("#url").val());
                request = request.replace(/\username/g, $("#username").val());
                request = request.replace(/\password/g, $("#password").val());
                request = request.replace(/\zzzz/g, $("#delay").val());
                }
                $("#request").val(request);
            });

                $("#send_customRPC-close").click(function() {
                        var method_name = $("#method").val();
                        var deviceID = $("#send_customRPC").attr('name');
                        var totalParam = '';
                        totalParam = "/devices/device_customRPC/"+deviceID+"/"+method_name;
                        window.location.href = totalParam;
                });

        $("#send_customRPC").click(function() {
            var object =  $("#object").val();
            var deviceID = $(this).attr('name');
            var commandKey =  $("#commandKey").val();
            var method_name = $("#method option:selected").val();
            var now = $('input[name=request]:checked', '#form_customRPC').val();
            if(object != "" && (method_name == "addObject" ||  method_name == "deleteObject"))
            {
            $.ajax({
                type: 'POST',
                url: "/devices/device_customRPC/"+deviceID+"/"+object+"/checkObject",
                success: function(AcsResponse) {
                    var checkObject = "";
                    $.each(AcsResponse.mapResult, function(id, result) {
                        if (result == 200) // success
                        {
                        $.ajax({
                            type: 'POST',
                            url: "/devices/device_customRPC/"+deviceID+"/"+method_name+"/"+now+"/send?object="+object,
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
                        else {
                        var alert_Exits = $('#alert_danger_input').attr('class');
                        $('#alert_danger span.text-semibold').text(alert_Exits);
                        $('#alert_danger').show();
                        }
                    });

                },
                error: function(data) {
                    console.log('Error ' + JSON.stringify(data));
                },
                async: true
            });
            } else if(method_name == "FactoryReset" || method_name == "Reboot") {
                if(commandKey == '') commandKey = 'null';
                        $.ajax({
                            type: 'POST',
                            url: "/devices/device_customRPC/"+deviceID+"/"+method_name+"/"+now+"/send?commandKey="+commandKey,
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
            } else if(method_name == "GetParameterNames") {
                    var object =  $("#parameterPath").val();
                    var level = $("#level").val();
                        $.ajax({
                            type: 'POST',
                            url: "/devices/device_customRPC/"+deviceID+"/"+method_name+"/"+now+"/send?object="+object+"&level="+level,
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
            } else if(method_name == "Upload") {
                    var isSend = true;
                    var fileType =  $("#fileType").val();
                    var url = $("#url").val();
                    var username = $("#username").val();
                    var password = $("#password").val();
                    var delay = $("#delay").val();
                    if(url == '' || url == null) {
                        var alert = $('#alert_danger_validate_input').attr('class');
                        $('#alert_danger_validate span.text-semibold').text('URL ' + alert);
                        $('#alert_danger_validate').show();
                        isSend = false;
                    } else {
                         if(url.startsWith("http://") == false && url.startsWith("ftp://") == false) {
                         var alert = $('#alert_danger_validate_url_input').attr('class');
                         $('#alert_danger_validate_url span.text-semibold').text('URL ' + alert);
                         $('#alert_danger_validate_url').show();
                         $("#url").val('');
                         isSend = false;
                       }
                    }
                    if(fileType == '' || fileType == null) {
                        var alert = $('#alert_danger_validate_input').attr('class');
                        $('#alert_danger_validate span.text-semibold').text('File type ' + alert);
                        $('#alert_danger_validate').show();
                        isSend = false;
                    }
                    if(isSend == true) {
                        $.ajax({
                            type: 'POST',
                            url: "/devices/device_customRPC/"+deviceID+"/"+method_name+"/"+now+"/send?commandKey="+commandKey+"&fileType="+fileType+"&url="+url+"&username="+username+"&password="+password+"&delay="+delay,
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

            } else if(method_name == "GetRPCMethod") {

                        $.ajax({
                            type: 'POST',
                            url: "/devices/device_customRPC/"+deviceID+"/"+method_name+"/"+now+"/send",
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

    firstLoadPage();

    function firstLoadPage() {
        initLibrary();
        getListParameters();
    }

    function initLibrary() {
        $('.add-parameter-input').autocomplete({
            source: []
        });
    }

    function getListParameters() {
        $.ajax({
            type: 'GET',
            url: location.origin + '/policy-configuration' + '/search-parameters',
            success: function (response) {
                parameters = response;
//                parametersByPath = [];
                parametersAutocomplete = [];
                $.each(response, function (index, value) {
//                    parametersByPath[value.path] = value;
                    parametersAutocomplete.push(value.path);
                });

                initAutocompleteParameter();
            }
        });
    }

    function initAutocompleteParameter() {
        parametersAutocomplete = [];
        $.each(parameters, function (index, value) {
            if(parametersSelected.indexOf(value.path) < 0) {
                parametersAutocomplete.push(value.path)
            }
        });
        form_rpc.find(".add-parameter-input").autocomplete('destroy').autocomplete({
            source: parametersAutocomplete
        });
    }

});