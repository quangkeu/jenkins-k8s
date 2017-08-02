$(function () {
    console.log('START LOGGING DEVICE');

    //<editor-fold desc="INIT LIBRARY">
    $('.fromDate').inputmask("yyyy-mm-dd hh:mm:ss");
    $('.toDate').inputmask("yyyy-mm-dd hh:mm:ss");
    //</editor-fold>

    //<editor-fold desc="FILTER INPUT">
    $('.fromDate').change(function () {
        validateDate();
    });
    $('.toDate').change(function () {
        validateDate();
    });

    function validateDate() {
        var fromDate = $('.fromDate').val();
        var toDate = $('.toDate').val();

        if (fromDate && toDate && fromDate !== '' && toDate !== '') {
            fromDate = new Date(fromDate);
            toDate = new Date(toDate);

            if (fromDate > toDate) {
                new PNotify({ text: 'From date must less than to date.', addclass: 'bg-danger'});
            }
        }
    }

    $('.filterActor').autocomplete({
        source: function (request, response) {
            $.ajax({
                type: "GET",
                url: location.origin + '/logging/device/search-device',
                data: {
                    deviceId: request.term
                },
                success: function (data) {
                    response(data);
                }
            });
        },
        minLength: 1
    });
    
    $('.formFilterLogging').submit(function (e) {
        e.preventDefault();
        beforeSubmit($(this));
    });

    function beforeSubmit(form) {
        var isValid = true;
        var messageError = '';

        // Validate special character
        var alNumRegexUsername = /[^0-9:-\s]/g; //only letters and numbers
        if (alNumRegexUsername.test(form.find('.fromDate').val())) {
            messageError += 'From date has special character. \n';
            isValid = false;
        }
        var alNumRegexUsername = /[^0-9:-\s]/g; //only letters and numbers
        if (alNumRegexUsername.test(form.find('.toDate').val())) {
            messageError += 'To date has special character. \n';
            isValid = false;
        }

        // Validate time
        validateDate();


        // Submit form
        if (form.valid() && isValid) {
            form.unbind('submit').submit().find('[type=submit]').trigger('click');
        } else {
            messageError = messageError === '' ? 'Fill all required.' : messageError;
            new PNotify({text: messageError, addclass: 'bg-danger'});
            return false;
        }
    }

    //</editor-fold>

    //<editor-fold desc="VIEW DETAIL CWMP">
    $('.rowSession').find('td').click(function () {
        var rowCwmpHtml = $(this).parent().parent('tbody').find('.rowCwmp');
        if(rowCwmpHtml.is(':visible')) {
            rowCwmpHtml.hide();
        } else {
            rowCwmpHtml.show();
        }
    });

    $('.rowCwmp').click(function () {
        $('.modalLoggingDeviceCwmp').modal('show');
        var currentCwmpMessageHtml = $(this).find('td.cwmpMessage').html();
        $('.currentCwmpMessage').html(currentCwmpMessageHtml);
    });

    $('.rowSession').find('td.unbindClick').unbind('click');
    //</editor-fold>

    //<editor-fold desc="EXPORT">
    $('.exportSession').click(function () {
        window.location.href = location.origin + location.pathname + '/export/' + $(this).parent().parent().attr('data-session');
    });
    //</editor-fold>

    //<editor-fold desc="PAGING">
    var dataPagination = $('.dataPagination');
    if(!dataPagination.attr('data-number')) {
        dataPagination.attr('data-number', 1)
    } else {
        ump.updateUrlQueryString('page', dataPagination.attr('data-number'));
    }
    $('.listPage').twbsPagination({
        totalPages: parseInt(dataPagination.attr('data-totalPages')) === 0 ? 1 : parseInt(dataPagination.attr('data-totalPages')),
        visiblePages: 4,
        startPage: parseInt(dataPagination.attr('data-number')),
        prev: '&#8672;',
        next: '&#8674;',
        first: '&#8676;',
        last: '&#8677;',
        onPageClick: function (event, page) {
            if(page !== parseInt(dataPagination.attr('data-number'))) {
                ump.updateUrlQueryString('limit', (document.location.search.search('limit') > 0) ? ump.getUrlQueryValue('limit') : null);
                ump.updateUrlQueryString('page', page, function () {
                    location.reload();
                });
            }
        }
    });
    $('.goPageBtn').click(function () {
        var goPageInput = parseInt($('.inputGoPage').val());
        goPageInput = (1 <= goPageInput && goPageInput <= parseInt(dataPagination.attr('data-totalPages'))) ? goPageInput : parseInt(dataPagination.attr('data-number'));
        ump.updateUrlQueryString('limit', (document.location.search.search('limit') > 0) ? ump.getUrlQueryValue('limit') : null);
        ump.updateUrlQueryString('page', goPageInput, function () {
            location.reload();
        });
    });
    $('.inputLimitPage').change(function () {
        var limitPageInput = parseInt($('.inputLimitPage').val());
        ump.updateUrlQueryString('limit', limitPageInput, function () {
            location.reload();
        });
    });
    //</editor-fold>

    //<editor-fold desc="REMOVE ALL">
    $('.btnDelete').click(function () {
        if (confirm('Do you want to delete all device logs?')) {
            $.ajax({
                type : "POST",
                url : location.origin + location.pathname + '/remove-all',
                data: {
                    name: $('.formFilterLogging').find('input[name=name]').val(),
                    actor: $('.formFilterLogging').find('input[name=actor]').val(),
                    fromDate: $('.formFilterLogging').find('input[name=fromDate]').val(),
                    toDate: $('.formFilterLogging').find('input[name=toDate]').val()
                },
                success : function(response) {
                    if (response) {
                        location.replace(document.location.origin + document.location.pathname);
                    }
                }
            });
        }
    });
    //</editor-fold>

    //<editor-fold desc="SETTING TIME EXPIRE">
    var modalLoggingDeviceSetting = $('.modalLoggingDeviceSetting');
    $('.btnSetting').click(function () {
        modalLoggingDeviceSetting.modal('show');

        $.ajax({
            type : "GET",
            url : location.origin + '/logging/get-time-expire',
            data: {},
            success : function(response) {
                if (response) {
                    modalLoggingDeviceSetting.find('.timeExpire').val(response)
                }

                modalLoggingDeviceSetting.unbind('click').find('.btnSave').click(function () {
                    var timeExpire = modalLoggingDeviceSetting.find('.timeExpire').val().trim();

                    var alNumRegexUsername = /[0-9]+[dwmy]{1}?/ig;
                    var timeExpireArr = timeExpire.match(alNumRegexUsername);
                    timeExpire = timeExpire.replace(/\s/g, '');

                    // 1d1w1m1y
                    if(timeExpireArr.join('').length !== timeExpire.length) {
                        new PNotify({ text: 'The time is not valid', addclass: 'bg-danger'});
                    } else {
                        $.ajax({
                            type : "POST",
                            url : location.origin + '/logging/post-save-time-expire',
                            data: {timeExpire: timeExpireArr.join(' ').toLowerCase()},
                            success : function() {
                                location.replace(document.location.origin + document.location.pathname);
                            }
                        });
                    }

                })

            }
        });
    });

    modalLoggingDeviceSetting.find('.btnCancel').click(function () {
        modalLoggingDeviceSetting.modal('hide');
    });
    //</editor-fold>

});