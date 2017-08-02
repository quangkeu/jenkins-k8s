

$(document).ready(function() {

        $("#send_file_downnload-close").click(function() {
            $('#file_type').val('');
            $('#file_version').html('');
            $("#requestNow").prop("checked", true)
        });

        $('#file_type').on('change', function (e) {
            $('#file_version').html('');
            var optionSelected = $("option:selected", this);
            var file_type = this.value;
            var deviceID = $(this).attr('class');
            if (file_type != '') {
                        $.ajax({
                            type: 'POST',
                            url: "/devices/device_file_download/getListFileVersions/"+deviceID+"/"+file_type,
                            success: function(data) {
                            $.each(data, function( k, v ) {
                                    $('#file_version').append($('<option>', {
                                        value: k,
                                        text: v
                                    }));
                            });
                            },
                            error: function(data) {
//                                console.log('Error ' + JSON.stringify(data));
                            },
                            async: true
                        });
                        }
        });

        $("#send_download_file").click(function() {
            var deviceID = $(this).attr('name');
            var file_version = $("#file_version option:selected").val();
            var file_name = $("#file_version option:selected").text();
            var now = $('input[name=request]:checked', '#form_send_file_download').val();
                        $.ajax({
                            type: 'POST',
                            url: "/devices/device_file_download/send/"+deviceID+"/"+file_version+"/"+file_name+"/"+now,
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
        });

                    $('.close').click(function() {
                        $(this).parent().hide();
                    });


});