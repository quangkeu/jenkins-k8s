$(function () {

    console.debug('START LIST POLICY');

    $('.policy-row').find('td').click(function () {
        var policyId = $(this).parent().attr('data-policyId');
        var actionName = $(this).parent().attr('data-actionName');
        if(actionName && actionName === 'parameters') {
            // Redirect to detail configuration
            location.replace(location.origin + '/policy-configuration/' + policyId);

        } else {
            // Redirect to detail operation
            location.replace(location.origin + '/policy-operation/' + policyId);
        }
    });
    $('.policy-row').find('td.unbind-click').unbind('click');

    //<editor-fold desc="DELETE POLICY">
    var policyIdsChecked = [];
    var remove_policy_opener = $('.remove-policy-opener');
    var remove_policy_opener_html = remove_policy_opener.html();
    function showRemoveButton(isShow) {
        if(isShow) {
            remove_policy_opener.show();
            remove_policy_opener.html(remove_policy_opener_html + ' ('+policyIdsChecked.length+')')
        } else {
            remove_policy_opener.hide();
        }
    }
    function updateCheckAllStatus() {
        var checked = null;
        var unChecked = null;
        $('.checkbox-row').each(function () {
            if ($(this).is(":checked")) {
                checked = true;
            } else {
                unChecked = true;
            }
        });

        if(checked != null && unChecked != null) {
            $('input.checkbox-all').prop('indeterminate', true);
        } else {
            $('input.checkbox-all').prop('indeterminate', false);
            if(checked) {
                $('input.checkbox-all').prop('checked', 'checked');
            } else if(unChecked) {
                $('input.checkbox-all').prop('checked', false);
            }
        }
    }

    function initCheckbox () {
        var queryChecked = ump.getUrlQueryValue('checked');
        if(queryChecked != null) {
            policyIdsChecked = queryChecked.split(',');
            $('.checkbox-row').each(function() {
                var idChecked = $(this).attr('data-policyId');
                if(policyIdsChecked.indexOf(idChecked) >= 0) {
                    $(this).prop('checked', true);
                }
            });
            showRemoveButton(policyIdsChecked.length > 0);
            updateCheckAllStatus();
        }
    }

    initCheckbox();
    showRemoveButton(policyIdsChecked.length > 0);

    function checkAll() {
        $('.checkbox-row').each(function() {
            var path = $(this).attr('data-policyId');
            $('input[data-policyId="'+path+'"]').prop('checked', true);
            if(policyIdsChecked.indexOf(path) < 0) {
                policyIdsChecked.push(path);
            }
        });
        showRemoveButton(policyIdsChecked.length > 0);
        ump.updateUrlQueryString('checked', policyIdsChecked.join(','));
    }
    function unCheckAll() {
        $('.checkbox-row').each(function() {
            var path = $(this).attr('data-policyId');
            $('input[data-policyId="'+path+'"]').prop('checked', false);
            policyIdsChecked.splice(policyIdsChecked.indexOf(path), 1);
        });
        showRemoveButton(policyIdsChecked.length > 0);
        ump.updateUrlQueryString('checked', policyIdsChecked.length > 0 ? policyIdsChecked.join(',') : null);
    }
    $('input.checkbox-all').change(function() {
        if ($(this).is(":checked")) {
            checkAll();
        } else {
            unCheckAll();
        }
    });
    $('input.checkbox-row').change(function() {
        var path = $(this).attr('data-policyId');
        if ($(this).is(":checked")) {
            policyIdsChecked.push(path);
        } else {
            policyIdsChecked.splice(policyIdsChecked.indexOf(path), 1);
        }
        showRemoveButton(policyIdsChecked.length > 0);
        updateCheckAllStatus();
        ump.updateUrlQueryString('checked', policyIdsChecked.length > 0 ? policyIdsChecked.join(',') : null);
    });

    remove_policy_opener.click(function () {
        if(confirm('Do you want to delete this policy?')) {
            var id = {};
            $.each(policyIdsChecked, function (index, value) {
                id[index] = value;
            });
            deletePolicy(id)
        }
    });

    $('.delete-btn').click(function () {
        if(confirm('Do you want to delete this policy?')) {
            deletePolicy($(this).attr('data-policyId'))
        }
    });

    function deletePolicy(id) {
        $.ajax({
            type : "POST",
            url : document.location.origin + '/policy/delete',
            data: { id: id},
            success : function() {
                location.replace(location.origin + location.pathname);
            }
        });
    }
    //</editor-fold>

    //<editor-fold desc="EXECUTE POLICY">
    $('.execute-btn').click(function () {
        if(confirm('Do you want to execute this policy?')) {
            $.ajax({
                type : "POST",
                url : document.location.origin + '/policy/{id}/execute'.split("{id}").join($(this).attr('data-policyId')),
                success : function() {
                    location.reload();
                }
            });
        }
    });
    //</editor-fold>

    //<editor-fold desc="STOP POLICY">
    $('.stop-btn').click(function () {
        if(confirm('Do you want to stop this policy?')) {
            $.ajax({
                type : "POST",
                url : document.location.origin + '/policy/{id}/stop'.split("{id}").join($(this).attr('data-policyId')),
                success : function() {
                    location.reload();
                }
            });
        }
    });
    //</editor-fold>

    //<editor-fold desc="PAGING POLICY">
    var paginator_data = $('#data-policy-paginator');
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