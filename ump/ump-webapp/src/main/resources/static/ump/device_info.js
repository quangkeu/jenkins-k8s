$(document).ready(function() {
    var rebootNowOrLast = true;
    var deviceIdTotal = $('#deviceIdParam').val();
    var specialChars = "<>@!#$%^&*()+[]{}?:;|'\"\\,./~`=";
    var check = function(string){
        for(i = 0; i < specialChars.length;i++){
            if(string.indexOf(specialChars[i]) > -1){
                return true
            }
        }
        return false;
    }
    loadEvent();
    function randomIntFromInterval(min,max)
    {
        return Math.floor(Math.random()*(max-min+1)+min);
    }
    $('[data-toggle="tooltip"]').tooltip();
    $('#add-tags-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: '50%',
        height: 'auto'
    });
    $('#add-tags-dialog-close').click(function() {
        $('#add-tags-dialog').dialog('close');
    });

    $('#add-tags-dialog-submit').click(function() {
        var tagName = $('#name-tag').val();
        if(tagName != ""){
            $.ajax({
                type: 'POST',
                url: "/devices/device_info/addTags",
                data: {
                    tagName: tagName+"@",
                    device: deviceIdTotal
                },
                success: function(response) {
                    if(response == 200){
                        var tagsSize = $('#tagsSize').val();
                        var parsedText;
                        var totalArray = JSON.parse(tagsSize);
                        var arr = tagName.split(",");
                        for(i=0; i<arr.length; i++){
                            var next_filter = arr[i];
                            var numberRandom;
                            do {
                                    numberRandom = randomIntFromInterval(10,100);
                                }
                            while($.inArray(numberRandom, totalArray) != -1);
                            totalArray.push(numberRandom);

                            var html_replace = '<span id="s'+numberRandom.toString()+'" class="tag label label-info" accept-charset="ISO-8859-1">'
                                                  +'<span class="labelNormal">'+next_filter+'</span>'
                                                  +'<a id="a'+numberRandom.toString()+'"><i class="remove glyphicon glyphicon-remove-sign glyphicon-white"></i></a></span>                                                        ';
                            $("#addlabelDiv").append(html_replace);
                        }
                        $('#tagsSize').val(JSON.stringify(totalArray));
                        $('#name-tag').val("");
                        $('#add-tags-dialog').dialog('close');
                        loadEvent();
                    }
                },
                async: true
            });
        }


    });
    $('#replace-cpe-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: '50%',
        height: 'auto'
    });
    $('#replace-cpe-dialog-close').click(function() {
        $('#replace-cpe-dialog').dialog('close');
    });


    $("#serial-replace-cpe1").keyup(function(event){
        if(event.keyCode == 13){
            getDevices();
        }
    });
    $('#replace-cpe-dialog-submit').click(function() {
        var serialNumber = $('#serial-replace-cpe1').val();
        if(serialNumber != "" && check(serialNumber) == false){
            $.ajax({
                type: 'GET',
                url: "/devices/device_info/"+deviceIdTotal+"/"+serialNumber+"/replaceCPE",
                success: function(response) {
                    var successAlert = "Replace CPE Successfully!";
                    if(response == 'successful'){
                        $('#replace-cpe-dialog').dialog('close');
                        $('#alert_success span.text-bold').text(successAlert);
                        $('#alert_success').show();
                    } else {
                        $('#alert_danger_replace_cpe span.text-bold').text(response+"");
                        $('#alert_danger_replace_cpe').show();
                    }
                },
                async: true
            });
        } else {
            $('#alert_danger_replace_cpe span.text-bold').text("Invalid SerialNumber");
            $('#alert_danger_replace_cpe').show();
        }
    });


function getDevices() {
    $("#listDevices").html('');
    var serial = $("#serial-replace-cpe1").val();
    var data1 = '';
    $.ajax({
        type: 'GET',
        url: "/devices/device_info/"+deviceIdTotal+"/"+serial+"/getDevices/",
        success: function(response) {
            data1 = JSON.parse(response);
            $(data1).each(function (idx, o) {
                carsOption = "<option value='" + this.id + "'>" + this.id + "</option>";
                $('#listDevices').append(carsOption);
            });
        },
        async: true
    });
}

    function loadEvent(){
     $("a").unbind("click").click(function(event) {
            if($(this).attr('id') == 'addLabel'){
                $('.ui-dialog-title').html('Add tags');
                $('#add-tags-dialog').dialog('open');
            } else if ($(this).attr('id') == 'rebootDeviceInfo') {
                $('.ui-dialog-title').html('Are you sure you want to reboot ?');
                $('#confirm-reboot-dialog').dialog('open');
            } else if ($(this).attr('id') == 'factoryResetDeviceInfo') {

                $('.ui-dialog-title').html('Are you sure you want to reset ?');
                $('#confirm-reset-dialog').dialog('open');
            } else if ($(this).attr('id') == 'deleteDeviceInfo') {

                $('.ui-dialog-title').html('Are you sure you want to delete ?');
                $('#confirm-delete-dialog').dialog('open');
            } else if ($(this).attr('id') == 'replaceCPEDeviceInfo') {
                $('#serial-replace-cpe1').val('');
                $('#alert_danger_replace_cpe').hide();
                $('.ui-dialog-title').html('Replace CPE');
                $('#replace-cpe-dialog').dialog('open');

                // alert_danger_btn

            } else if ($(this).attr('id') == 'alert_danger_btn') {

                $('#alert_danger').hide();
            } else if ($(this).attr('id') == 'alert_danger_replace_cpe_btn') {

                $('#alert_danger_replace_cpe').hide();
            } else if ($(this).attr('id') == 'alert_primary_btn') {

                $('#alert_primary').hide();
            }  else if ($(this).attr('id') == 'alert_success_btn') {

                $('#alert_success').hide();
            } else if ($(this).attr('id') == 'confirm-recheck-status-dialog-opener') {
                buttonLoading("confirm-recheck-status-dialog-opener","start");
                $.ajax({
                    type: 'GET',
                    url: "/devices/device_info/"+deviceIdTotal+"/recheckStatus",
                    timeout : 60000,
                    success: function(response) {
                        var successAlert = "Recheck Status Successfully!";
                        var failedAlert = "Recheck Status Failed!";
                        $('#alert_danger').hide();
                        buttonLoading("confirm-recheck-status-dialog-opener","stop");
                        if(response == true){
                            $('#alert_success span.text-bold').text(successAlert);
                            $('#alert_success').show();
                        } else {
                            $('#alert_danger span.text-bold').text(failedAlert);
                            $('#alert_danger').show();
                        }
                    },
                    async: true
                });
                $('#replace-cpe-dialog').dialog('close');
            } else if ($(this).attr('id') == 'displayProvisioningTags') {
            } else if ($(this).attr('id') == 'ping-device') {
                var stringUrl = document.getElementById("connectionRequestId").innerText;
                var ipDevice = stringUrl.substring(7,stringUrl.length-1);
                var dotPosition = ipDevice.indexOf(":");
                ipDevice = ipDevice.substring(0,dotPosition);
                $.ajax({
                    type: 'GET',
                    url: "/devices/device_info/"+ipDevice.trim()+"/pingDevice",
                    timeout : 60000,
                    success: function(response) {
                        alert(response);
                    },
                    async: true
                });
            }
            else {
                var idDeleteLabel = $(this).attr('id');
                if(idDeleteLabel != null){
                    var sID = '#s'+$(this).attr('id').substring(1);
                    var sName = $(sID).text();
                    $.ajax({
                        type: 'POST',
                        url: "/devices/device_info/deleteLabel",
                        data: {
                            tagName : sName,
                            device: deviceIdTotal
                        },
                        success: function(response) {
                            if(response){
                                $(sID).remove();
                                var tagsSize = $('#tagsSize').val();
                                var totalArray = JSON.parse(tagsSize);
                                remove(totalArray,idDeleteLabel.substring(1));
                                $('#tagsSize').val(JSON.stringify(totalArray));
                            }
                        },
                        async: true
                    });
                }
            }

        });
    }
function remove(array, value) {
    var i, j, cur;
    for (i = array.length - 1; i >= 0; i--) {
        cur = array[i];
        if (cur == value) {
            array.splice(i, 1);
        }
    }
}
    // REBOOT
    $('#confirm-reboot-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: '50%',
        height: 'auto'
    });
    $('#confirm-reboot-dialog-close').click(function() {
        $('#confirm-reboot-dialog').dialog('close');
    });

    function getUrlParameter(sParam) {
        var sPageURL = decodeURIComponent(window.location.search.substring(1)),
            sURLVariables = sPageURL.split('&'),
            sParameterName,
            i;

        for (i = 0; i < sURLVariables.length; i++) {
            sParameterName = sURLVariables[i].split('=');

            if (sParameterName[0] === sParam) {
                return sParameterName[1] === undefined ? true : sParameterName[1];
            }
        }
    };
    $('#confirm-reboot-now-dialog').click(function() {
        $.ajax({
            type: 'GET',
            url: "/devices/device_info/" + deviceIdTotal + "/rebootDevice",
            success: function(result) {
                var successAlert = "Reboot successfully! The device is rebooting, please waiting for 2 minutes";
                var inprogresAlert = "Reboot in queue!";
                var failedAlert = "Reboot Failed!";
                $('#alert_danger').hide();
                if(result == 200){
                    $('#alert_success span.text-bold').text(successAlert);
                    $('#alert_success').show();
                } else if(result == 202){
                    $('#alert_primary span.text-bold').text(inprogresAlert);
                    $('#alert_primary').show();
                } else {
                    $('#alert_danger span.text-bold').text(failedAlert);
                    $('#alert_danger').show();
                }
            },
            async: true
        });

        $('#confirm-reboot-dialog').dialog('close');
    });
/// End Reboot

// Factory Reset
    $('#confirm-reset-dialog').dialog({
        autoOpen: false,
        modal: true,
        height: 'auto',
        width: '50%'
    });

    $('#confirm-reset-dialog-close').click(function() {
        $('#confirm-reset-dialog').dialog('close');
    });

    $('#confirm-reset-now-dialog').click(function() {
        $.ajax({
            type: 'GET',
            url: "/devices/device_info/" + deviceIdTotal + "/factoryResetDevice",
            success: function(result) {
                var successAlert = "Reset successfully! The device is reseting, please waitting for 2 minutes";
                var inprogresAlert = "Factory Reset in queue!";
                var failedAlert = "Factory Reset Failed!";
                $('#alert_danger').hide();
                if(result == 200){
                    $('#alert_success span.text-bold').text(successAlert);
                    $('#alert_success').show();
                } else if(result == 202){
                    $('#alert_primary span.text-bold').text(inprogresAlert);
                    $('#alert_primary').show();
                } else {
                    $('#alert_danger span.text-bold').text(failedAlert);
                    $('#alert_danger').show();
                }
            },
            async: true
        });
        $('#confirm-reset-dialog').dialog('close');
    });
// END Factory Reset

// Delete
    $('#confirm-delete-dialog').dialog({
        autoOpen: false,
        modal: true,
        height: 'auto',
        width: '50%'
    });
    $('#confirm-delete-device-dialog-temporarily').click(function() {
        deleteObj("temporarily");
    });
    $('#confirm-delete-device-dialog-permanently').click(function() {
        deleteObj("permanently");
    });
    function deleteObj(mode){
        var deviceID = $('#deviceInfoID').val();
        $.ajax({
            type: 'GET',
            url: "/devices/device_info/"+deviceID+"/deleteDevice/"+ mode,
            success: function(response) {
                if(response){
                    window.location = "/";
                }
            },
            async: true
        });
    }
// END Delete
});