$(function () {
    // START PROVISIONING VIEW BY TAG

    // <editor-fold desc="LIST PROVISIONING BY TAG VIEW">
    $('.device-type-version-select2').select2({
        ajax: {
            url: document.location.origin + '/provisioning/search-device-type-version',
            dataType: 'json',
            delay: 250,
            data: function (params) {
                return {
                    q: params.term, // search term
                    page: params.page
                };
            },
            processResults: function (data, params) {
                var inputOptions = [];
                $.each(data, function (index, value) {
                    var optGroup = { text: index, children: []};
                    $.each(value, function (index2, value2) {
                        optGroup.children.push({ id: value2.id, text: value2.firmwareVersion});
                    });
                    if(optGroup.children.length > 0) {
                        inputOptions.push(optGroup);
                    }
                });
                return { results: inputOptions};
            },
            cache: true
        }

    }).on('select2:select', function (e) {
        // Save new subscriber device mapping device
        var rootTagId = $(this).attr('data-rootTagId');
        var deviceTypeVersionIdInsert = e.params.data.id;
        $.ajax({
            type: 'POST',
            url: document.location.origin + '/provisioning/post-save-device-type-version',
            data: {
                rootTagId: rootTagId,
                deviceTypeVersionId: deviceTypeVersionIdInsert
            },
            success: function (data) {
                if(data) {
                    location.reload();
                } else {
                    alert(e.params.data.text+' is existed.');
                    e.preventDefault();
                }

            }
        })


    }).on('select2:unselecting', function (e) {
        if(!confirm('Are you sure to delete '+e.params.args.data.text+'?')) {
            e.preventDefault();

        } else {
            var rootTagId = $(this).attr('data-rootTagId');
            var deviceTypeVersionIdRemove = $(this).find('option[value="'+e.params.args.data.id+'"]').attr('data-deviceTypeVersionId');
            $.ajax({
                type: 'POST',
                url: document.location.origin + '/provisioning/post-delete-device-type-version',
                data: {
                    rootTagId: rootTagId,
                    deviceTypeVersionId: deviceTypeVersionIdRemove
                },
                success: function () {
                    location.reload();
                }
            })

        }

    });
    //</editor-fold>

    //<editor-fold desc="DELETE PROVISIONING BY TAG VIEW">
    var removeIdByTagCheckedArr = [];
    var remove_provisioning_by_tag_opener = $('#remove-provisioning-by-tag-opener');
    var remove_provisioning_by_tag_opener_html = remove_provisioning_by_tag_opener.html();
    var table_provisioning_by_tag = $('.table-provisioning-by-tag');
    function showRemoveButton(isShow) {
        if(isShow) {
            remove_provisioning_by_tag_opener.show();
            remove_provisioning_by_tag_opener.html(remove_provisioning_by_tag_opener_html + ' ('+removeIdByTagCheckedArr.length+')')
        } else {
            remove_provisioning_by_tag_opener.hide();
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
            $('input#checkbox-all').prop('indeterminate', true);
        } else {
            $('input#checkbox-all').prop('indeterminate', false);
            if(checked) {
                $('input#checkbox-all').prop('checked', 'checked');
            } else if(unChecked) {
                $('input#checkbox-all').prop('checked', false);
            }
        }
    }

    function initCheckboxByTag () {
        var queryChecked = ump.getUrlQueryValue('checked');
        if(queryChecked != null) {
            removeIdByTagCheckedArr = queryChecked.split(',');
            $('.checkbox-row').each(function() {
                var idChecked = $(this).attr('data-removeId');
                if(removeIdByTagCheckedArr.indexOf(idChecked) >= 0) {
                    $(this).prop('checked', true);
                }
            });
            showRemoveButton(removeIdByTagCheckedArr.length > 0);
            updateCheckAllStatus();
        }
    }

    initCheckboxByTag();
    showRemoveButton(removeIdByTagCheckedArr.length > 0);
    function checkAll() {
        table_provisioning_by_tag.find('.checkbox-row').each(function() {
            var path = $(this).attr('data-removeId');
            $('input[data-removeId="'+path+'"]').prop('checked', true);
            if(removeIdByTagCheckedArr.indexOf(path) < 0) {
                removeIdByTagCheckedArr.push(path);
            }
        });
        showRemoveButton(removeIdByTagCheckedArr.length > 0);
        ump.updateUrlQueryString('checked', removeIdByTagCheckedArr.join(','));
    }
    function unCheckAll() {
        table_provisioning_by_tag.find('.checkbox-row').each(function() {
            var path = $(this).attr('data-removeId');
            table_provisioning_by_tag.find('input[data-removeId="'+path+'"]').prop('checked', false);
            removeIdByTagCheckedArr.splice(removeIdByTagCheckedArr.indexOf(path), 1);
        });
        showRemoveButton(removeIdByTagCheckedArr.length > 0);
        ump.updateUrlQueryString('checked', removeIdByTagCheckedArr.length > 0 ? removeIdByTagCheckedArr.join(',') : null);
    }
    table_provisioning_by_tag.find('input#checkbox-all').change(function() {
        if ($(this).is(":checked")) {
            checkAll();
        } else {
            unCheckAll();
        }
    });
    table_provisioning_by_tag.find('input.checkbox-row').change(function() {
        var path = $(this).attr('data-removeId');
        if ($(this).is(":checked")) {
            removeIdByTagCheckedArr.push(path);
        } else {
            removeIdByTagCheckedArr.splice(removeIdByTagCheckedArr.indexOf(path), 1);
        }
        showRemoveButton(removeIdByTagCheckedArr.length > 0);
        updateCheckAllStatus();
        ump.updateUrlQueryString('checked', removeIdByTagCheckedArr.length > 0 ? removeIdByTagCheckedArr.join(',') : null);
    });

    remove_provisioning_by_tag_opener.click(function () {
        var nameRemove = [];
        var ids = {};
        $.each(removeIdByTagCheckedArr, function (index, value) {
            ids['id_' + index] = value;
            nameRemove.push(table_provisioning_by_tag.find('.checkbox-row[data-removeId='+value+']').attr('data-removeName'));
        });

        var messageRemove = 'Do you want to delete ' + nameRemove.join(', ') + '?';
        if(confirm(messageRemove)) {
            $.ajax({
                type : "POST",
                url : document.location.origin + '/provisioning/post-delete-provisioning-by-tag',
                data: { id: removeIdByTagCheckedArr},
                success : function(data) {
                    location.replace(location.origin + location.pathname);
                }
            });
        }
    });
    //</editor-fold>


});