$(document).ready(function() {

    /*
       remocce loadding icon in tab
    */
    $("#_tabDiagnostic i").remove();

    /**
    validate form input for ping
    */
    $('#_diagnosticForm').validate({ // initialize the plugin
        rules: {
            _diagnosticHost: {
                required: true,
                maxlength : 30
            },
            _diagnosticBlockSize: {
                required: true,
                number: true,
                minlength: 1,
                maxlength : 10
            },
            _diagnosticTimeOut: {
                required: true,
                number: true,
                minlength: 1,
                maxlength : 5
            },
            _diagnosticNumberOfRepetitions: {
                required: true,
                number: true,
                minlength: 1,
                maxlength : 3
            }
        },
        messages: {
            _diagnosticHost: "Please enter your host, maximum 30 character",
            _diagnosticBlockSize: "Please enter your block size, must be number",
            _diagnosticTimeOut: "Please enter your timeout,  must be number",
            _diagnosticNumberOfRepetitions: "Please enter your number of repetitions , must be number"
        },
        submitHandler: function() {
            var dataToPost = $('#_diagnosticForm').serializeArray();
            dataToPost.push({
                name: 'deviceId',
                value: $('#deviceId').text()
            });
            var iconTurnAround = '<i class="icon-spinner9 spinner position-right"></i>';
            $('#btnStartTest').append(iconTurnAround);
            $('#btnStartTest').addClass('disabled');
            $('#btnStartTest').prop("disabled", true);
            $.ajax({
                type: 'POST',
                url: "/diagnostic/ping",
                data: JSON.stringify(dataToPost),
                success: function(response, status) {
                    console.dir(response);
                    console.log("status " + status);
                    $("#diagnostics_tab").html(response);
                    $('#_diagnosticAlertSuccess span.text-semibold').text("Ping Success!");
                    $('#_diagnosticAlertSuccess').show(2000);


                },
                error: function(data, status) {
                    console.log("failed" + status);
                    console.dir(data);
                    $('#btnStartTest i').remove();
                    $("#btnStartTest").removeClass("disabled");
                    $("#btnStartTest").removeAttr("disabled");
                    $('#_diagnosticAlertPrimary span.text-semibold').text("Ping Failed!");
                    $('#_diagnosticAlertPrimary').show(2000);
                },
                async: true
            });
            return false; // for demo
        }
    });

    $('.close').click(function() {
        $('#_diagnosticAlertSuccess').hide();
        $('#_diagnosticAlertPrimary').hide();
    });
});