$(function () {
    console.log('START CHANGE PASSWORD');

    //<editor-fold desc="Handle submit form">
    var formItem = $('.formChangePassword'),
        formItemValidator = formItem.validate(ump.validator);

    formItem.find('.inputRepeatPassword').on('change keyup paste', function () {
        var currentPassword = formItem.find('.inputPassword').val(),
            repeatPassword = formItem.find('.inputRepeatPassword').val();
        if (currentPassword !== repeatPassword) {
            formItem.find('.submitBtn').attr('disabled', true);
            formItem.find('.messageInputPasswordError').show();
        } else {
            formItem.find('.submitBtn').removeAttr('disabled');
            formItem.find('.messageInputPasswordError').hide();
        }

    });

    formItem.find('.inputPassword').on('change keyup paste', function () {
        var currentPassword = formItem.find('.inputPassword').val(),
            repeatPassword = formItem.find('.inputRepeatPassword').val();
        if (repeatPassword !== '' && currentPassword !== repeatPassword) {
            formItem.find('.submitBtn').attr('disabled', true);
            formItem.find('.messageInputPasswordError').show();
        } else {
            formItem.find('.submitBtn').removeAttr('disabled');
            formItem.find('.messageInputPasswordError').hide();
        }

    });

    formItem.submit(function (e) {
        e.preventDefault();
        beforeSubmit(formItem);
    });

    function beforeSubmit(form) {
        var isValid = true,
            messageError = '',
            currentPassword = formItem.find('input[name=currentPassword]').val(),
            currentUsername = formItem.find('input[name=username]').val();

        // Message validate input
        $.ajax({
            type: "POST",
            url: document.location.origin + '/user/post-check-current-password',
            data: {
                username: currentUsername,
                currentPassword: currentPassword
            },
            success: function (response) {
                if (response == null || !response) {
                    messageError = "Current password is wrong";
                    isValid = false;
                }

                // Submit form
                if(form.valid() && isValid) {
                    form.unbind('submit').submit().find('[type=submit]').trigger('click');
                } else {
                    messageError = messageError === '' ? 'Fill all required.' : messageError;
                    new PNotify({text: messageError, addclass: 'bg-danger'});
                    return false;
                }
            }
        });
    }
    //</editor-fold>

});