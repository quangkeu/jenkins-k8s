$(function() {

    //<editor-fold desc="LIST PROVISIONING BY DEVICE VIEW">
    $('.tag-select2').select2({
        "language": {
            "noResults": function () {
                return "Add new";
            }
        },
        ajax: {
            url: document.location.origin + '/provisioning/search-root-tag',
            dataType: 'json',
            delay: 250,
            data: function (params) {
                return {
                    q: params.term, // search term
                    page: params.page
                };
            },
            processResults: function (data, params) {
                params.page = params.page || 1;
                var result = $.map(data, function (item) {
                    return { text: item.name, id: item.name}
                });

                // Check to add new option
                if(result.length === 0) {
                    result.push({text: params.term + ' (Add New)', id: params.term})
                }

                return { results: result};
            },
            cache: true
        }

    }).on('select2:select', function (e) {
        // Save new subscriber device mapping device
        var deviceTypeVersionId = $(this).attr('data-deviceTypeVersionId');
        var tagName = e.params.data.id;
        $.ajax({
            type: 'POST',
            url: document.location.origin + '/provisioning/post-save-root-tag',
            data: {
                tagName: tagName,
                deviceTypeVersionId: deviceTypeVersionId
            },
            success: function (data) {

                if(data) {
                    location.reload();
                } else {
                    alert(e.params.data.text+' is existed.')
                    e.preventDefault();
                }

            }
        })


    }).on('select2:unselecting', function (e) {
        if(!confirm('Are you sure to delete '+e.params.args.data.text+'?')) {
            e.preventDefault();
        } else {
            var tagIdRemove = $(this).find('option[value="'+e.params.args.data.text+'"]').attr('data-tagId');

            $.ajax({
                type: 'POST',
                url: document.location.origin + '/provisioning/post-delete-root-tag',
                data: {
                    tagId: tagIdRemove
                },
                success: function () {
                    location.reload();
                }
            })

        }

    });
    //</editor-fold>

    //<editor-fold desc="DELETE PROVISIONING BY DEVICE VIEW">
    var removeIdCheckedArr = [];
    var remove_subscriber_opener = $('#remove-provisioning-opener');
    var remove_subscriber_opener_html = remove_subscriber_opener.html();
    function showRemoveButton(isShow) {
        if(isShow) {
            remove_subscriber_opener.show();
            remove_subscriber_opener.html(remove_subscriber_opener_html + ' ('+removeIdCheckedArr.length+')')
        } else {
            remove_subscriber_opener.hide();
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

    function initCheckbox () {
        var queryChecked = ump.getUrlQueryValue('checked');
        if(queryChecked != null) {
            removeIdCheckedArr = queryChecked.split(',');
            $('.checkbox-row').each(function() {
                var idChecked = $(this).attr('data-removeId');
                if(removeIdCheckedArr.indexOf(idChecked) >= 0) {
                    $(this).prop('checked', true);
                }
            });
            showRemoveButton(removeIdCheckedArr.length > 0);
            updateCheckAllStatus();
        }
    }

    initCheckbox();
    showRemoveButton(removeIdCheckedArr.length > 0);
    function checkAll() {
        $('.checkbox-row').each(function() {
            var path = $(this).attr('data-removeId');
            $('input[data-removeId="'+path+'"]').prop('checked', true);
            if(removeIdCheckedArr.indexOf(path) < 0) {
                removeIdCheckedArr.push(path);
            }
        });
        showRemoveButton(removeIdCheckedArr.length > 0);
        ump.updateUrlQueryString('checked', removeIdCheckedArr.join(','));
    }
    function unCheckAll() {
        $('.checkbox-row').each(function() {
            var path = $(this).attr('data-removeId');
            $('input[data-removeId="'+path+'"]').prop('checked', false);
            removeIdCheckedArr.splice(removeIdCheckedArr.indexOf(path), 1);
        });
        showRemoveButton(removeIdCheckedArr.length > 0);
        ump.updateUrlQueryString('checked', removeIdCheckedArr.length > 0 ? removeIdCheckedArr.join(',') : null);
    }
    $('input#checkbox-all').change(function() {
        if ($(this).is(":checked")) {
            checkAll();
        } else {
            unCheckAll();
        }
    });
    $('input.checkbox-row').change(function() {
        var path = $(this).attr('data-removeId');
        if ($(this).is(":checked")) {
            removeIdCheckedArr.push(path);
        } else {
            removeIdCheckedArr.splice(removeIdCheckedArr.indexOf(path), 1);
        }
        showRemoveButton(removeIdCheckedArr.length > 0);
        updateCheckAllStatus();
        ump.updateUrlQueryString('checked', removeIdCheckedArr.length > 0 ? removeIdCheckedArr.join(',') : null);
    });

    remove_subscriber_opener.click(function () {
        var nameRemove = [];
        var ids = {};
        $.each(removeIdCheckedArr, function (index, value) {
            ids['id_' + index] = value;
            nameRemove.push($('.tableProvisioningByDevice').find('.checkbox-row[data-removeId='+value+']').attr('data-removeName'));
        });

        var messageRemove = 'Do you want to delete ' + nameRemove.join(', ') + '?';
        if(confirm(messageRemove)) {
            $.ajax({
                type : "POST",
                url : document.location.origin + '/provisioning/post-delete-provisioning-by-device-type-version',
                data: { id: removeIdCheckedArr},
                success : function(data) {
                    location.replace(location.origin + location.pathname);
                }
            });
        }
    });
    //</editor-fold>

});