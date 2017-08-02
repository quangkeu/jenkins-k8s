$(function () {
    console.debug('START LIST POLICY LOG');

    //<editor-fold desc="PAGING POLICY">
    var paginator_data = $('#data-policy-log-paginator');
    $('.twbs-prev-next').twbsPagination({
        totalPages: parseInt(paginator_data.attr('data-totalPages')),
        visiblePages: 4,
        startPage: parseInt(paginator_data.attr('data-number')),
        prev: '&#8672;',
        next: '&#8674;',
        first: '&#8676;',
        last: '&#8677;',
        onPageClick: function (event, page) {
            if(page !== parseInt(paginator_data.attr('data-number'))) {
                ump.updateUrlQueryString('limit', (document.location.search.search('limit') > 0) ? ump.getUrlQueryValue('limit') : null);
                ump.updateUrlQueryString('page', page, function () {
                    location.reload();
                });
            }
        }
    });
    $('.go-page-btn').click(function () {
        var goPageInput = parseInt($('.go-page-input').val());
        goPageInput = (1 <= goPageInput && goPageInput <= parseInt(paginator_data.attr('data-totalPages'))) ? goPageInput : parseInt(paginator_data.attr('data-number'));
        ump.updateUrlQueryString('limit', (document.location.search.search('limit') > 0) ? ump.getUrlQueryValue('limit') : null);
        ump.updateUrlQueryString('page', goPageInput, function () {
            location.reload();
        });
    });
    $('.limit-page-input').change(function () {
        var limitPageInput = parseInt($('.limit-page-input').val());
        ump.updateUrlQueryString('limit', limitPageInput, function () {
            location.reload();
        });
    });
    if(ump.getUrlQueryValue('limit') && parseInt(ump.getUrlQueryValue('limit')) > 0) {
        $('.limit-page-input').val(ump.getUrlQueryValue('limit'));
    }
    //</editor-fold>

});