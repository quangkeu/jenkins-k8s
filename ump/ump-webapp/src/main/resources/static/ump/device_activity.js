$(function () {

    //<editor-fold desc="INIT LIBRARY">
    $('.fromDateTime').inputmask("yyyy-mm-dd hh:mm:ss");
    $('.toDateTime').inputmask("yyyy-mm-dd hh:mm:ss");
    //</editor-fold>

    var dataRemoveChecked = [];
    var btnDelete = $('.btnDelete');
    var btnDelete_html = $('.btnDelete').html();
    function showRemoveButton(isShow) {
        if(isShow) {
            btnDelete.show();
            btnDelete.html(btnDelete_html + ' ('+dataRemoveChecked.length+')')
        } else {
            btnDelete.hide();
        }
    }

    function updateCheckAllStatus() {
        var checked = null;
        var unChecked = null;
        $('.checkboxRow').each(function () {
            if ($(this).is(":checked")) {
                checked = true;
            } else {
                unChecked = true;
            }
        });

        if(checked != null && unChecked != null) {
            $('.checkboxAll').prop('indeterminate', true);
        } else {
            $('.checkboxAll').prop('indeterminate', false);
            if(checked) {
                $('.checkboxAll').prop('checked', 'checked');
            } else if(unChecked) {
                $('.checkboxAll').prop('checked', false);
            }
        }
    }

    function initCheckbox () {
        var queryChecked = ump.getUrlQueryValue('checked');
        if(queryChecked != null) {
            dataRemoveChecked = queryChecked.split(',');
            $('.checkboxRow').each(function() {
                var idChecked = $(this).attr('data-id');
                if(dataRemoveChecked.indexOf(idChecked) >= 0) {
                    $(this).prop('checked', true);
                }
            });
            showRemoveButton(dataRemoveChecked.length > 0);
            updateCheckAllStatus();
        }
    }

    initCheckbox();
    showRemoveButton(dataRemoveChecked.length > 0);

    function checkAll() {
        $('.checkboxRow').each(function() {
            var path = $(this).attr('data-id');
            $('input[data-id="'+path+'"]').prop('checked', true);
            if(dataRemoveChecked.indexOf(path) < 0) {
                dataRemoveChecked.push(path);
            }
        });
        showRemoveButton(dataRemoveChecked.length > 0);
        ump.updateUrlQueryString('checked', dataRemoveChecked.join(','));
    }
    function unCheckAll() {
        dataRemoveChecked = [];
        $('.checkboxRow').each(function() {
            var path = $(this).attr('data-id');
            $('input[data-id="'+path+'"]').prop('checked', false);
            dataRemoveChecked.splice(dataRemoveChecked.indexOf(path), 1);
        });
        showRemoveButton(dataRemoveChecked.length > 0);
        ump.updateUrlQueryString('checked', dataRemoveChecked.length > 0 ? dataRemoveChecked.join(',') : null);
    }
    $('.checkboxAll').change(function() {
        if ($(this).is(":checked")) {
            checkAll();
        } else {
            unCheckAll();
        }
    });
    $('.checkboxRow').change(function() {
        var path = $(this).attr('data-id');
        if ($(this).is(":checked")) {
            dataRemoveChecked.push(path);
        } else {
            dataRemoveChecked.splice(dataRemoveChecked.indexOf(path), 1);
        }
        showRemoveButton(dataRemoveChecked.length > 0);
        updateCheckAllStatus();
        ump.updateUrlQueryString('checked', dataRemoveChecked.length > 0 ? dataRemoveChecked.join(',') : null);
    });

    btnDelete.click(function () {
        if (confirm('ARE YOU SURE TO DELETE?')) {
            $.ajax({
                type: 'POST',
                url: location.origin + '/devices/device_activity/delete',
                data: {ids: dataRemoveChecked.join(",")},
                success: function (response) {
                    if (response) {
                        location.replace(location.origin + location.pathname);
                    }
                }
            });
        }
    });


    //<editor-fold desc="FILTER INPUT">
    enabledDateTimeFilter($('.fromDateTime').val() != '' || $('.toDateTime').val() != '');
    $('.formFilterLogging').find('input[name="showFilter"]').change(function () {
        var currentType = $(this).val();
        if (currentType === 'showByDate') {
            enabledDateTimeFilter(true);

        } else if (currentType === 'showAll') {
            enabledDateTimeFilter(false);
        }
    });

    function enabledDateTimeFilter(isEnabled) {
        if (isEnabled) {
            $('.formFilterLogging').find('input[name="showFilter"][value="showByDate"]').attr('checked', true);
            $('.fromDateTime').attr('disabled', false);
            $('.toDateTime').attr('disabled', false);

        } else {
            $('.formFilterLogging').find('input[name="showFilter"][value="showAll"]').attr('checked', true);
            $('.fromDateTime').attr('disabled', true);
            $('.toDateTime').attr('disabled', true);
        }
    }

    $('.fromDateTime').change(function () {
        validateDate();
    });
    $('.toDateTime').change(function () {
        validateDate();
    });

    function validateDate() {
        var fromDateTime = $('.fromDateTime').val();
        var toDateTime = $('.toDateTime').val();

        if (fromDateTime && toDateTime && fromDateTime !== '' && toDateTime !== '') {
            fromDateTime = new Date(fromDateTime);
            toDateTime = new Date(toDateTime);

            if (fromDateTime > toDateTime) {
                new PNotify({ text: 'From date must less than to date.', addclass: 'bg-danger'});
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="PAGING">
    var dataPagination = $('.dataPagination');
    if(!dataPagination.attr('data-number')) {
        dataPagination.attr('data-number', 1)
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

});