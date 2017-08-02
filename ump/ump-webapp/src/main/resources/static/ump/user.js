$(function () {
    console.debug('START USER');

    //<editor-fold desc="INDEX">

    //<editor-fold desc="FILTER">
    initFilter();

    function initFilter() {

        // Fill data to input
        $('.formFilter').find('.selectDeviceGroup').val($('.formFilter').find('input[name=device_group_ids]').val().split(',')).change();

        // Init lib
        $('.selectDeviceGroup').select2({
            placeholder: '- Select device group -',
            dropdownCssClass: 'border-ddd',
            containerCssClass: 'border-ddd'
        });
    }
    //</editor-fold>


    //<editor-fold desc="FILTER USER">
    $('.selectDeviceGroup').change(function () {
        beforeFilter($('.formFilter'));
    });
    
    $('.formFilter').submit(function (e) {
        e.preventDefault();
        beforeFilter($(this));
    });

    function beforeFilter(form) {
        if (form.find('.selectDeviceGroup').val()) {
            form.find('input[name=device_group_ids]').val(form.find('.selectDeviceGroup').val().join(',')).change();
        } else {
            form.find('input[name=device_group_ids]').val('');
        }

        // Submit form
        if(form.valid()) {
            form.unbind('submit').submit().find('[type=submit]').trigger('click');
        }
    }
    //</editor-fold>


    //<editor-fold desc="RESET PASSWORD">
    var modalConfirmResetPassword = $('.modalConfirmResetPassword');
    $('.resetPasswordItemBtn').click(function () {
        modalConfirmResetPassword.modal('show');
        var currentId = $(this).attr('data-id'),
            labelUserName = '',
            labelEmail = '';

        $('.tableList').find('tr').each(function () {
            if(currentId === $(this).attr('data-id')) {
                labelUserName = $(this).find('td.colUserName').text();
                labelEmail = $(this).find('td.colEmail').text();
            }
        });

        modalConfirmResetPassword.find('.labelResetPassword').html('Would like you to recover ' + labelUserName + '\'s password ' +
            '& the security code will send it to: <strong>' + labelEmail + '</strong>');

        modalConfirmResetPassword.find('.submitBtn').unbind('click').click(function () {
            postResetPassword(currentId);
        });

    });

    function postResetPassword(userId) {
        $.ajax({
            type : "POST",
            url : document.location.origin + '/user/'+userId+'/post-reset-password',
            success : function() {
                location.reload();
            }
        });
    }
    //</editor-fold>

    //<editor-fold desc="PAGING">
    var dataPagination = $('.dataPagination');
    if (ump.getUrlQueryValue('page')) {
        ump.updateUrlQueryString('page', dataPagination.attr('data-number'));
    }
    $('.listPage').twbsPagination({
        totalPages: parseInt(dataPagination.attr('data-totalPages')),
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
    if(ump.getUrlQueryValue('limit') && parseInt(ump.getUrlQueryValue('limit')) > 0) {
        $('.inputLimitPage').val(ump.getUrlQueryValue('limit'));
    }
    //</editor-fold>

    //<editor-fold desc="REMOVE">
    var checkboxAll = $('.checkboxAll'),
        checkboxRow = $('.checkboxRow'),
        checkedIds = [],
        removeBtn = $('.removeBtn'),
        removeBtnHtml = removeBtn.html();

    initCheckbox();

    checkboxAll.change(function() {
        if ($(this).is(":checked")) {
            checkAll();
        } else {
            unCheckAll();
        }
    });

    checkboxRow.change(function() {
        var currentId = $(this).attr('data-id');
        if ($(this).is(":checked")) {
            checkedIds.push(currentId);
        } else {
            checkedIds.splice(checkedIds.indexOf(currentId), 1);
        }
        showRemoveButton(checkedIds.length > 0);
        updateCheckAllStatus();
        ump.updateUrlQueryString('checked', checkedIds.length > 0 ? checkedIds.join(',') : null);
    });

    removeBtn.click(function () {
        var modalConfirmRemove = $('.modalConfirmRemove'),
            labelConfirm = 'Do you want to delete: ';
        modalConfirmRemove.modal('show');

        var id = {};
        $.each(checkedIds, function (index, value) {
            id[index] = value;

            $('.tableList').find('tr').each(function () {
                if(value === $(this).attr('data-id')) {
                    labelConfirm += $(this).find('td.colUserName').text() + ', ';
                }
            });
        });
        labelConfirm = labelConfirm.substr(0, labelConfirm.length - 2);
        modalConfirmRemove.find('.labelResetPassword').text(labelConfirm + '?');

        modalConfirmRemove.find('.submitBtn').unbind('click').click(function () {
            removeItem(id)
        });

    });

    $('.removeItemBtn').click(function () {
        var modalConfirmRemove = $('.modalConfirmRemove'),
            labelConfirm = 'Do you want to delete: ',
            currentId = $(this).attr('data-id');
        modalConfirmRemove.modal('show');

        $('.tableList').find('tr').each(function () {
            if(currentId === $(this).attr('data-id')) {
                labelConfirm += $(this).find('td.colUserName').text() + ', ';
            }
        });
        labelConfirm = labelConfirm.substr(0, labelConfirm.length - 2);
        modalConfirmRemove.find('.labelResetPassword').text(labelConfirm + '?');
        modalConfirmRemove.find('.submitBtn').unbind('click').click(function () {
            removeItem(currentId)
        });
    });

    function removeItem(id) {
        $.ajax({
            type : "POST",
            url : document.location.origin + '/user/post-remove',
            data: { ids: id},
            success : function() {
                location.replace(document.location.origin + document.location.pathname);
            }
        });
    }

    function showRemoveButton(isShow) {
        if(isShow) {
            removeBtn.show();
            removeBtn.html(removeBtnHtml + ' ('+checkedIds.length+')')
        } else {
            removeBtn.hide();
        }
    }

    function updateCheckAllStatus() {
        var checked = null;
        var unChecked = null;
        checkboxRow.each(function () {
            if ($(this).is(":checked")) {
                checked = true;
            } else {
                unChecked = true;
            }
        });

        if(checked != null && unChecked != null) {
            checkboxAll.prop('indeterminate', true);
        } else {
            checkboxAll.prop('indeterminate', false);
            if(checked) {
                checkboxAll.prop('checked', 'checked');
            } else if(unChecked) {
                checkboxAll.prop('checked', false);
            }
        }
    }

    function initCheckbox () {
        var queryChecked = ump.getUrlQueryValue('checked');
        if(queryChecked != null) {
            checkedIds = queryChecked.split(',');
            checkboxRow.each(function() {
                var idChecked = $(this).attr('data-id');
                if(checkedIds.indexOf(idChecked) >= 0) {
                    $(this).prop('checked', true);
                }
            });

            showRemoveButton(checkedIds.length > 0);
            updateCheckAllStatus();
        } else {
            showRemoveButton(false);
        }
    }

    function checkAll() {
        checkboxRow.each(function() {
            var currentId = $(this).attr('data-id');
            $('input[data-id="'+currentId+'"]').prop('checked', true);
            if(checkedIds.indexOf(currentId) < 0) {
                checkedIds.push(currentId);
            }
        });
        showRemoveButton(checkedIds.length > 0);
        ump.updateUrlQueryString('checked', checkedIds.join(','));
    }

    function unCheckAll() {
        checkboxRow.each(function() {
            var currentId = $(this).attr('data-id');
            $('input[data-id="'+currentId+'"]').prop('checked', false);
            checkedIds.splice(checkedIds.indexOf(currentId), 1);
        });
        showRemoveButton(checkedIds.length > 0);
        ump.updateUrlQueryString('checked', checkedIds.length > 0 ? checkedIds.join(',') : null);
    }

    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="CREATE & EDIT">
    var modalForm = $('.modalFormUser'),
        formItem = $('.formUser'),
        formItemValidator = formItem.validate(ump.validator);

    //<editor-fold desc="Open modal form">
    $('.createBtn').click(function () {
        modalForm.modal('show');
        formItem.find('.titleCreate').show();
        formItem.find('.titleEdit').hide();
        formItem.attr('action', '/user/store');
        formItem.find('input[name=user_name]').attr('check-api', '/user/0/get-existed-username?username=');
        formItem.find('input[name=email]').attr('check-api', '/user/0/get-existed-email?email=');
        resetForm();
    });

    $('.editItemBtn').click(function () {
        updateItem($(this).attr('data-id'));
    });

    $('.itemRow').find('td').click(function () {
        var editUserId = $(this).parent().attr('data-id');
        updateItem(editUserId);
    });

    $('.itemRow').find('td.unbindClick').unbind('click');

    function updateItem(editUserId) {
        modalForm.modal('show');
        formItem.find('.titleEdit').show();
        formItem.find('.titleCreate').hide();
        formItem.attr('action', '/user/' + editUserId + '/update');
        formItem.find('input[name=user_name]').attr('check-api', '/user/' + editUserId + '/get-existed-username?username=');
        formItem.find('input[name=email]').attr('check-api', '/user/' + editUserId + '/get-existed-email?email=');
        resetForm();
        $.ajax({
            type : "GET",
            url : document.location.origin + '/user/' + editUserId + '/get-edit',
            success : function(response) {
                if(response !== null) {
                    formItem.find('input[name=user_name]').val(response.userName);
                    formItem.find('input[name=full_name]').val(response.fullName);
                    formItem.find('input[name=email]').val(response.email);
                    formItem.find('input[name=phone]').val(response.phone);
                    formItem.find('textarea[name=description]').val(response.description);
                    formItem.find('select[name=select_role]').val(response.roleIds).change();
                    formItem.find('select[name=select_group_filter]').val(response.deviceGroupIds).change();
                }
            }
        });
    }
    //</editor-fold>

    //<editor-fold desc="Handle form event">
    formItem.find('.selectGroupFilter').select2({
        placeholder: '- Select device group -',
        dropdownCssClass: 'border-ddd',
        containerCssClass: 'border-ddd'
    });

    formItem.find('.selectRole').select2({
        placeholder: '- Select role -',
        dropdownCssClass: 'border-ddd',
        containerCssClass: 'border-ddd'
    });

    formItem.find('.fileAvatar').fileinput({
        browseLabel: 'Choose Image',
        browseIcon: '<i class="icon-file-plus"></i>',
        uploadIcon: '<i class="icon-file-upload2"></i>',
        removeIcon: '<i class="icon-minus3"></i>',
        layoutTemplates: {
            icon: '<i class="icon-file-check"></i>'
        },
        initialPreview: [
            // TODO EDIT
            //"<img src='/public/admin/assets/images/placeholder.jpg' class='file-preview-image' alt=''>",
        ],
        initialCaption: 'Choose Image',
        allowedFileExtensions: ["jpg", "png"]
    });

    formItem.find('.fileAvatar').bind('change', function() {
        var currentSize = this.files[0].size;
        if(currentSize > 2097152) {
            formItem.find('.messageFileAvatarError').show();
        } else {
            formItem.find('.messageFileAvatarError').hide();
        }
    });
    //</editor-fold>

    //<editor-fold desc="Handle submit form">
    formItem.submit(function (e) {
        e.preventDefault();
        beforeSubmit(formItem);
    });

    function resetForm() {
        formItem.find('.select2').val('').change();
        formItem.trigger('reset');
        formItemValidator.resetForm();
    }

    function beforeSubmit(form) {
        var isValid = true,
            messageError = '',
            currentRole = form.find('.selectRole').val(),
            currentGroupDevice = form.find('.selectGroupFilter').val(),
            currentFile = form.find('.fileAvatar')[0].files[0],
            currentSize = 0;

        // Set default value
        currentRole = currentRole !== null ? currentRole : [];
        currentGroupDevice = currentGroupDevice !== null ? currentGroupDevice : [];
        currentSize = currentFile ? currentFile.size : currentSize;

        // Append html input
        form.find('.inputGroupDevice').val(currentGroupDevice.join(','));
        form.find('.inputRole').val(currentRole.join(','));

        // Message validate input
        if(currentRole.length === 0) {
            messageError += 'Role is not empty. \n';
            isValid = false;
        }
        if(currentGroupDevice.length === 0) {
            messageError += 'Group device is not empty. \n';
            isValid = false;
        }
        if(currentSize > 2097152) {
            messageError += 'The uploaded file exceeds the max size 2MB. \n';
            isValid = false;
        }

        // Validate special character
        var alNumRegexUsername = /[^a-zA-Z0-9_.]/g; //only letters and numbers
        if (alNumRegexUsername.test(formItem.find('input[name=user_name]').val())) {
            messageError += 'Username has special character. \n';
            isValid = false;
        }

        var alNumRegexPhone = /[^0-9+]/g; //only letters and numbers
        if (alNumRegexPhone.test(formItem.find('input[name=phone]').val())) {
            messageError += 'Phone has special character. \n';
            isValid = false;
        }

        // Submit form
        if(form.valid() && isValid) {
            form.unbind('submit').submit().find('[type=submit]').trigger('click');
        } else {
            messageError = messageError === '' ? 'Fill all required.' : messageError;
            new PNotify({ text: messageError, addclass: 'bg-danger'});
            return false;
        }
    }
    //</editor-fold>
    //</editor-fold>

});