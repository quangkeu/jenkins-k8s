
$(document).ready(function() {

        var downdload = "<cwmp:Download xmls:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n  <CommandKey>_cm_</CommandKey>\n  <FileType>_filetype_</FileType>\n  <URL>url</URL>\n  <Username>username</Username>\n  <Password>password</Password>\n  <FileSize>_filesize_</FileSize>\n  <TargetFileName>_target_</TargetFileName>\n  <DelaySeconds>_delay_</DelaySeconds>\n  <SuccessURL>_success_</SuccessURL>\n  <FailureURL>_failure_</FailureURL>\n </cwmp:Download>";

        $("#commandKey").on("propertychange change keyup paste input", function(){
           var request = "";
           var method_name = $("#method").val();
           var object =  $(this).val();
           if(method_name == "Download") {
                request = downdload.replace(/\_cm_/g, object);
                request = request.replace(/\_filetype_/g, $("#fileType").val());
                request = request.replace(/\url/g, $("#url").val());
                request = request.replace(/\username/g, $("#username").val());
                request = request.replace(/\password/g, $("#password").val());
                request = request.replace(/\_delay_/g, $("#delay").val());
                request = request.replace(/\_filesize_/g, $("#fileSize").val());
                request = request.replace(/\_target_/g, $("#target").val());
                request = request.replace(/\_success_/g, $("#successURL").val());
                request = request.replace(/\_failure_/g, $("#failureURL").val());
           }
           $("#request").val(request);
        });

        $("#url").on("propertychange change keyup paste input", function(){
           var request = "";
           var method_name = $("#method").val();
           var url =  $(this).val();
           if(method_name == "Download") {
                request = downdload.replace(/\_cm_/g, $("#commandKey").val());
                request = request.replace(/\_filetype_/g, $("#fileType").val());
                request = request.replace(/\url/g, url);
                request = request.replace(/\username/g, $("#username").val());
                request = request.replace(/\password/g, $("#password").val());
                request = request.replace(/\_delay_/g, $("#delay").val());
                request = request.replace(/\_filesize_/g, $("#fileSize").val());
                request = request.replace(/\_target_/g, $("#target").val());
                request = request.replace(/\_success_/g, $("#successURL").val());
                request = request.replace(/\_failure_/g, $("#failureURL").val());
           }
           $("#request").val(request);
        });

        $("#username").on("propertychange change keyup paste input", function(){
           var request = "";
           var method_name = $("#method").val();
           var username =  $(this).val();
           if(method_name == "Download") {
                request = downdload.replace(/\_cm_/g, $("#commandKey").val());
                request = request.replace(/\_filetype_/g, $("#fileType").val());
                request = request.replace(/\url/g, $("#url").val());
                request = request.replace(/\username/g, username);
                request = request.replace(/\password/g, $("#password").val());
                request = request.replace(/\_delay_/g, $("#delay").val());
                request = request.replace(/\_filesize_/g, $("#fileSize").val());
                request = request.replace(/\_target_/g, $("#target").val());
                request = request.replace(/\_success_/g, $("#successURL").val());
                request = request.replace(/\_failure_/g, $("#failureURL").val());
           }
           $("#request").val(request);
        });

        $("#password").on("propertychange change keyup paste input", function(){
           var request = "";
           var method_name = $("#method").val();
           var password =  $(this).val();
           if(method_name == "Download") {
                request = downdload.replace(/\_cm_/g, $("#commandKey").val());
                request = request.replace(/\_filetype_/g, $("#fileType").val());
                request = request.replace(/\url/g, $("#url").val());
                request = request.replace(/\username/g, $("#username").val());
                request = request.replace(/\password/g, password);
                request = request.replace(/\_delay_/g, $("#delay").val());
                request = request.replace(/\_filesize_/g, $("#fileSize").val());
                request = request.replace(/\_target_/g, $("#target").val());
                request = request.replace(/\_success_/g, $("#successURL").val());
                request = request.replace(/\_failure_/g, $("#failureURL").val());
           }
           $("#request").val(request);
        });

        $("#delay").on("propertychange change keyup paste input", function(){
           var request = "";
           var method_name = $("#method").val();
           var delay =  $(this).val();
           if(method_name == "Download") {
                request = downdload.replace(/\_cm_/g, $("#commandKey").val());
                request = request.replace(/\_filetype_/g, $("#fileType").val());
                request = request.replace(/\url/g, $("#url").val());
                request = request.replace(/\username/g, $("#username").val());
                request = request.replace(/\password/g, $("#password").val());
                request = request.replace(/\_delay_/g, delay);
                request = request.replace(/\_filesize_/g, $("#fileSize").val());
                request = request.replace(/\_target_/g, $("#target").val());
                request = request.replace(/\_success_/g, $("#successURL").val());
                request = request.replace(/\_failure_/g, $("#failureURL").val());
           }
           $("#request").val(request);
        });

        $("#fileSize").on("propertychange change keyup paste input", function(){
           var request = "";
           var method_name = $("#method").val();
           var fileSize =  $(this).val();
           if(method_name == "Download") {
                request = downdload.replace(/\_cm_/g, $("#commandKey").val());
                request = request.replace(/\_filetype_/g, $("#fileType").val());
                request = request.replace(/\url/g, $("#url").val());
                request = request.replace(/\username/g, $("#username").val());
                request = request.replace(/\password/g, $("#password").val());
                request = request.replace(/\_delay_/g, $("#delay").val());
                request = request.replace(/\_filesize_/g, fileSize);
                request = request.replace(/\_target_/g, $("#target").val());
                request = request.replace(/\_success_/g, $("#successURL").val());
                request = request.replace(/\_failure_/g, $("#failureURL").val());
           }
           $("#request").val(request);
        });

        $("#target").on("propertychange change keyup paste input", function(){
           var request = "";
           var method_name = $("#method").val();
           var target =  $(this).val();
           if(method_name == "Download") {
                request = downdload.replace(/\_cm_/g, $("#commandKey").val());
                request = request.replace(/\_filetype_/g, $("#fileType").val());
                request = request.replace(/\url/g, $("#url").val());
                request = request.replace(/\username/g, $("#username").val());
                request = request.replace(/\password/g, $("#password").val());
                request = request.replace(/\_delay_/g, $("#delay").val());
                request = request.replace(/\_filesize_/g, $("#fileSize").val());
                request = request.replace(/\_target_/g, target);
                request = request.replace(/\_success_/g, $("#successURL").val());
                request = request.replace(/\_failure_/g, $("#failureURL").val());
           }
           $("#request").val(request);
        });

        $("#successURL").on("propertychange change keyup paste input", function(){
           var request = "";
           var method_name = $("#method").val();
           var successURL =  $(this).val();
           if(method_name == "Download") {
                request = downdload.replace(/\_cm_/g, $("#commandKey").val());
                request = request.replace(/\_filetype_/g, $("#fileType").val());
                request = request.replace(/\url/g, $("#url").val());
                request = request.replace(/\username/g, $("#username").val());
                request = request.replace(/\password/g, $("#password").val());
                request = request.replace(/\_delay_/g, $("#delay").val());
                request = request.replace(/\_filesize_/g, $("#fileSize").val());
                request = request.replace(/\_target_/g, $("#target").val());
                request = request.replace(/\_success_/g, successURL);
                request = request.replace(/\_failure_/g, $("#failureURL").val());
           }
           $("#request").val(request);
        });

        $("#failureURL").on("propertychange change keyup paste input", function(){
           var request = "";
           var method_name = $("#method").val();
           var failureURL =  $(this).val();
           if(method_name == "Download") {
                request = downdload.replace(/\_cm_/g, $("#commandKey").val());
                request = request.replace(/\_filetype_/g, $("#fileType").val());
                request = request.replace(/\url/g, $("#url").val());
                request = request.replace(/\username/g, $("#username").val());
                request = request.replace(/\password/g, $("#password").val());
                request = request.replace(/\_delay_/g, $("#delay").val());
                request = request.replace(/\_filesize_/g, $("#fileSize").val());
                request = request.replace(/\_target_/g, $("#target").val());
                request = request.replace(/\_success_/g, $("#successURL").val());
                request = request.replace(/\_failure_/g, failureURL);
           }
           $("#request").val(request);
        });

            $("#fileType").change(function(){
                var request = "";
                var method_name = $("#method").val();
                var fileType =  $(this).val();
                if(method_name == "Download") {
                request = downdload.replace(/\_cm_/g, $("#commandKey").val());
                request = request.replace(/\_filetype_/g, fileType);
                request = request.replace(/\url/g, $("#url").val());
                request = request.replace(/\username/g, $("#username").val());
                request = request.replace(/\password/g, $("#password").val());
                request = request.replace(/\_delay_/g, $("#delay").val());
                request = request.replace(/\_filesize_/g, $("#fileSize").val());
                request = request.replace(/\_target_/g, $("#target").val());
                request = request.replace(/\_success_/g, $("#successURL").val());
                request = request.replace(/\_failure_/g, failureURL);
                }
                $("#request").val(request);
            });


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
            var isSend = true;
            var deviceID = $(this).attr('name');
            var commandKey =  $("#commandKey").val();
            var method_name = $("#method option:selected").val();
            var now = $('input[name=request]:checked', '#form_customRPC').val();
            if(method_name == "Download") {
                    var fileType =  $("#fileType").val();
                    var url = $("#url").val();
                    var username = $("#username").val();
                    var password = $("#password").val();
                    var delay = $("#delay").val();
                    var fileSize =  $("#fileSize").val();
                    var target =  $("#target").val();
                    var successURL =  $("#successURL").val();
                    var failureURL =  $("#failureURL").val();
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
                        isSend = false;
                        var alert = $('#alert_danger_validate_input').attr('class');
                        $('#alert_danger_validate span.text-semibold').text('File type ' + alert);
                        $('#alert_danger_validate').show();
                    }

                    if(isSend == true) {
                        $.ajax({
                            type: 'POST',
                            url: "/devices/device_customRPC/"+deviceID+"/"+method_name+"/"+now+"/send?commandKey="+commandKey+"&fileType="+fileType+"&url="+url+"&username="+username+"&password="+password+"&delay="+delay+"&fileSize="+fileSize+"&target="+target+"&successURL="+successURL+"&failureURL="+failureURL,
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

});