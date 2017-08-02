$(function () {
    
    $('.forgotPasswordBtn').click(function () {
        $('.modalForgotPassword').modal('show');

        $('.searchBtn').unbind('click').click(function () {
            var inputEmail = $('.modalForgotPassword').find('input[name="email"]').val();

            $.ajax({
                type: "GET",
                url: document.location.origin + '/user/0/get-existed-email',
                data: {email: inputEmail},
                success: function (response) {
                    if (response !== null && response) {

                        $('.modalForgotPassword').modal('hide');
                        $('.modalConfirmForgotPassword').modal('show');
                        $('.modalConfirmForgotPassword').find('.inputEmail').html(inputEmail);

                        // Submit
                        $('.btnContinue').unbind('click').click(function () {
                            $.ajax({
                                type: "POST",
                                url: document.location.origin + '/user/forgot-password-with-email',
                                data: {email: inputEmail},
                                success: function () {
                                    location.replace('/login')
                                }
                            });
                        });

                        $('.messageNotFound').hide();

                    } else {
                        $('.messageNotFound').show();
                    }
                }
            });



        })
    })
    
});