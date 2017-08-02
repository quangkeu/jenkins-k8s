$(document).ready(function() {
    $("#selectDiagnosticType").on("change",function(){
        var diagnosticKey = $(this).val().replace(/(:|\.|\[|\]|,|=|@)/g, '\\$1');
        if(diagnosticKey !== '') {
        $(".diagnosticParameter").hide();
        $(".diagnosticParameter input").prop('required',false);
        $(".diagnosticParameter."+diagnosticKey).show();
        $(".diagnosticParameter."+diagnosticKey+" input").prop('required',true);
        } else $(".diagnosticParameter").hide();
    })
    $('#selectDiagnosticType option:first-child').attr("selected", "selected").trigger("change");

        $("#create-diagnostics-dialog-close").click(function() {
            window.location = $(this).data("href");
        });

    // ADVANCE SEARCH
    $('.select2').select2({
        allowClear: true
    });

    var ipformat = /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
    var domainformat = /^[a-zA-Z0-9][a-zA-Z0-9-]{1,61}[a-zA-Z0-9](?:\.[a-zA-Z]{2,})$/;
    $("#Host").focusout(function() {
        var url = $(this).val();

        if(url.match(ipformat) == null && url.match(domainformat) == null) {
           var alert = $('#alert_danger_validate_url_input').attr('class');
           $('#alert_danger_validate_url span.text-semibold').text('URL ' + alert);
           $('#alert_danger_validate_url').show();
           $("#Host").val('');
        }

      });

                    $('.close').click(function() {
                        $(this).parent().hide();
                    });

});