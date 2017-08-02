/**
this function block a div and wait an interaction to this
*/
function showLoading(idDiv) {
    $("#" + idDiv).block({
        message: '<i class="icon-spinner4 spinner"></i>',
        timeout: 120000, //unblock after 1 minutes
        overlayCSS: {
            backgroundColor: '#fff',
            opacity: 0.8,
            cursor: 'wait'
        },
        css: {
            border: 0,
            padding: 0,
            backgroundColor: 'transparent'
        }
    });
}
/**
this to create animation loading for button

*/
function buttonLoading(idButton, status) {
    if (status === 'start') {
        var iconTurnAround = '<i class="icon-spinner9 spinner position-right"></i>';
        $('#' + idButton).append(iconTurnAround);
        $('#' + idButton).addClass('disabled');
    } else if (status = 'end') {
        $('#' + idButton).addClass('enable');
        $('#' + idButton + ' i').remove();
        $('#' + idButton).removeClass("disabled");
    }
}

var warning = '<span style="color:red;"> This field is required!</span>';

function valiadateInputForm(input) {
    if (input.name !== null) {
        if (input.value.replace(/\s/g, '').toUpperCase() === 'NULL' || input.value.replace(/\s/g, '') === '') {
            input.defaultValue = input.value;
            input.outerHTML = input.outerHTML.replace(input.defaultValue, input.value) + warning;
            return true;
        }

    }
    return false;
}

function clearWarning(idForm) {
    // reset text warning
    var spans = $('#' + idForm).find('span');
    for (var i = 0; i < spans.size(); i++) {
        var span = spans.get(i);
        span.outerHTML = "";
    }
}