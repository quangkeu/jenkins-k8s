

$(document).ready(function() {

        $("#send_file_upload-close").click(function() {
            $('#file_type').val('');
            $("#requestNow").prop("checked", true)
        });

        $("#send_upload_file").click(function() {
            var deviceID = $(this).attr('name');
            var file_type = $("#file_type option:selected").val();
            if(file_type == "1 Firmware Upgrade Image") file_type = "Firmware upload";
            if(file_type == "3 Vendor Configuration File") file_type = "Vendor configuration file upload";
            var now = $('input[name=request]:checked', '#form_send_file_upload').val();
            if(file_type !== '') {
                        $.ajax({
                            type: 'POST',
                            url: "/devices/device_file_upload/send/"+deviceID+"/"+file_type+"/"+now,
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

});