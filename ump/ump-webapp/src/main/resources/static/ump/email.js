$(document).ready(function() {

    var paginator_data = $('#data-email-paginator');
    var indexPage = parseInt(paginator_data.attr('data-number'));
    var lastPagesVar = parseInt(paginator_data.attr('data-lastPage'));
    var startPageVar = parseInt(paginator_data.attr('data-number'));
    var limitPageInput = parseInt($('.limit-page-input').val());
    var totalEmailBeChecked = 0;
    var deleteIds = [];
    var confirmDeleteEmail = $('#delete-multi-email');
    var confirmDeleteEmail_html = confirmDeleteEmail.html();

    $('#add-new-email').removeClass('disabled');
    $('#update-email').addClass('disabled');

    $('#add-new-email').click(function() {
        markupStr = $('#summernote').summernote('code');
        id = $('#id-email').val();
        description = $('#description-email').val();

        $.ajax({
            type: 'POST',
            url: '/email-add-new',
            data: {
                id: id,
                description: description,
                value: markupStr,
                mode: 'add'
            },
            success: function(response) {
                if(response){
                    location.reload();
                }
            },
            async: true
        });
    });

    $('.email-edit-opener').click(function() {

        $('#add-new-email').addClass('disabled');
        $('#update-email').removeClass('disabled');

        $('#id-email').val($(this).attr('data-id'));
        $('#description-email').val($(this).attr('data-description'));
        $('#summernote').summernote('code',$(this).attr('data-value'));
    });

    $('#update-email').click(function() {
        markupStr = $('#summernote').summernote('code');
        id = $('#id-email').val();
        description = $('#description-email').val();

        $.ajax({
            type: 'POST',
            url: '/email-add-new',
            data: {
                id: id,
                description: description,
                value: markupStr,
                mode: 'update'
            },
            success: function(response) {
                if(response){
                    location.reload();
                }
            },
            async: true
        });
    });

    $('#delete-multi-email').click(function() {
        deleteIds = [];
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            if ($(this).is(':checked')) {
                deleteIds.push(id);
            }
        });
        if(deleteIds.length > 0){
        $('.ui-dialog-title').html('Do you want to delete ?');
        $('#confirm-delete-dialog').dialog('open');}
    });

    $('#confirm-delete-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: '50%',
        height: 'auto'
    });

    $('#confirm-delete-now-dialog').click(function() {
        $.ajax({
            type: 'POST',
            url: "/delete-email",
            data: {
                deleteIds: deleteIds+"@"
            },
            success: function(response) {
                if(response){
                    location.reload();
                }
            },
            async: true
        });

    });
    $('#confirm-delete-dialog-close').click(function() {
        $('#confirm-delete-dialog').dialog('close');
    });

    
    $('#checkAll').change(function() {
        if ($(this).is(":checked")) {
            checkAll();
        } else {
            unCheckAll();
        }
    });

    $('.myCheckBox').change(function() {
        if ($(this).is(":checked")) {
            totalEmailBeChecked = totalEmailBeChecked + 1;
        } else {
            totalEmailBeChecked = totalEmailBeChecked - 1;
        }

        if (totalEmailBeChecked > 0) {
            showGroupButton(true);
        } else {
            showGroupButton(false)
        }
    });

    $('.myCheckBox').each(function() {
        if ($(this).is(':checked')) {
            totalEmailBeChecked++;
        } else {
            if(totalEmailBeChecked != 0){totalEmailBeChecked--;}
        }
    });

    if (totalEmailBeChecked > 0) {
        showGroupButton(true);
    } else {
        showGroupButton(false)
    };

    function checkAll() {
        totalEmailBeChecked =0;
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            $('#' + id).prop('checked', true);
           totalEmailBeChecked++;
        });
        showGroupButton(true);
    }

    function unCheckAll() {
        totalEmailBeChecked = 0;
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            $('#' + id).prop('checked', false);
        });
        showGroupButton(false);
    }

    function showGroupButton(enable) {
        if (enable) {
//            confirmDeleteEmail.bind( "click" );
            confirmDeleteEmail.removeClass("disabled");
            confirmDeleteEmail.html(confirmDeleteEmail_html + ' ('+totalEmailBeChecked+')');
        } else {
            confirmDeleteEmail.html(confirmDeleteEmail_html);
            confirmDeleteEmail.addClass("disabled");
//            confirmDeleteEmail.unbind( "click" );
        }
    }

    $('.twbs-prev-next').twbsPagination({
        totalPages: lastPagesVar,
        visiblePages: 4,
        startPage: startPageVar,
        prev: '&#8672;',
        next: '&#8674;',
        first: '&#8676;',
        last: '&#8677;',
        onPageClick: function (event, page) {
            if(page !== parseInt(paginator_data.attr('data-number'))) {
                var limitPageInput = parseInt($('.limit-page-input').val());
                pagingDataModel(page, limitPageInput);

            }
        }
    });
    $('.go-page-btn').click(function () {
        var goPageInputVar = $('.go-page-input').val();
        if(goPageInputVar.indexOf("-") >= 0
            || goPageInputVar.indexOf("+") >= 0 || goPageInputVar.indexOf(".") >= 0
            || goPageInputVar == ""){
                $('#alert_danger span.text-bold').text("Invalid Number!");
                $('#alert_danger').show();
        } else {
            var goPageInput = parseInt($('.go-page-input').val());
            var limitPageInput = parseInt($('.limit-page-input').val());
            if(0 < goPageInput && goPageInput <= lastPagesVar){
                pagingDataModel(goPageInput, limitPageInput);
            } else {
                $('#alert_danger span.text-bold').text("Invalid Number!");
                $('#alert_danger').show();
            }
        }

    });
    $('.limit-page-input').change(function () {
        var limitPageInput = parseInt($('.limit-page-input').val());
        var indexPage = parseInt(paginator_data.attr('data-number'));
        pagingDataModel(1, limitPageInput);

    });

    function pagingDataModel(index, limit) {
        if(isNaN(index)){
            index = '1';
        }
        if(isNaN(limit)){
            limit = '20';
        }
        window.location.href = "/email/search?indexPage=" + index + "&limit=" + limit;
    }
});