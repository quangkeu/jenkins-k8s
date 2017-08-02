$(function(){
    $(".tree-default").fancytree({
        icon: false,
        click: function(event, data) {
            if(data.node.data.href && typeof data.node.data.href !== 'undefined') {
                window.location.replace(document.location.origin + data.node.data.href);
            }
        }
    });

    $('#create-device-type-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: 500
    });

    $('#create-device-type-dialog-opener').click(function() {
        $('form.form-validate-jquery').validate().resetForm();
        $('#create-device-type-dialog').dialog('open');
    });

    $('.show-device-type-actions').hover(
        function() {
            $(this).find('.device-type-action-add').show();
        },
        function() {
            $(this).find('.device-type-action-add').hide();
        }
    );
     $('#create_device-dialog-close').click(function() {
        $('#create-device-type-dialog').dialog('close');
        $('#form_create_device_type')[0].reset();
     });

   $('#confirm-delete-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: 500
    });

    $('#confirm-delete-dialog-opener').click(function() {
        $('#confirm-delete-dialog').dialog('open');
    });
     $('#confirm-delete-dialog-close').click(function() {
           $('#confirm-delete-dialog').dialog('close');
     });

    //validate form
    $("#update-device-type").validate(ump.validator);


    // ADVANCE SEARCH
    $('.select2').select2({
        allowClear: true
    });
    $('.dropdown-menu').click(function (e) {
        e.stopPropagation();
    });

    var urlParams;
    (window.onpopstate = function () {
        var match,
            pl = /\+/g,  // Regex for replacing addition symbol with a space
            search = /([^&=]+)=?([^&]*)/g,
            decode = function (s) { return decodeURIComponent(s.replace(pl, " ")); },
            query = window.location.search.substring(1);
        urlParams = {};
        while (match = search.exec(query))
            urlParams[decode(match[1])] = decode(match[2]);
    })();

    $.get(document.location.origin + '/device-type/get-list', function (data) {

        var manufacturerSelectBox = $('select[name="Manufacturer"]');
        var productClassSelectBox = $('select[name="Product Class"]');
        var firmwareVersionSelectBox = $('select[name="Firmware version"]');

        var listManufacturer = [];
        var listProductClass = [];
        var listVersion = [];
        var listDeviceTypeId = [];
        $.each(data.deviceType, function (key, value) {

            // Get list device type id
            if(typeof listDeviceTypeId[value.manufacturer] === 'undefined') {
                listDeviceTypeId[value.manufacturer] = [];
            }
            if(typeof listDeviceTypeId[value.manufacturer][value.productClass] === 'undefined') {
                listDeviceTypeId[value.manufacturer][value.productClass] = [];
            }
            if ($.inArray(value.id, listDeviceTypeId[value.manufacturer][value.productClass]) === -1) {
                listDeviceTypeId[value.manufacturer][value.productClass].push(value.id)
            }

            // Get list manufacturer
            if ($.inArray(value.manufacturer, listManufacturer) === -1) {
                listManufacturer.push(value.manufacturer);
            }

            // Get list product class
            if(typeof listProductClass[value.manufacturer] === 'undefined') {
                listProductClass[value.manufacturer] = [];
            }
            if ($.inArray(value.manufacturer, listProductClass[value.manufacturer]) === -1) {
                listProductClass[value.manufacturer].push(value.productClass)
            }
        });

        $.each(data.deviceTypeVersion, function (key, value) {
            // Get list device type version id
            if(typeof listVersion[value.deviceTypeId] === 'undefined') {
                listVersion[value.deviceTypeId] = [];
            }
            if ($.inArray(value.firmwareVersion, listVersion[value.deviceTypeId]) === -1) {
                listVersion[value.deviceTypeId].push(value.firmwareVersion)
            }
        });

        // Add data for manufacturer select box
        manufacturerSelectBox.html('<option></option>');
        $.each(listManufacturer, function (key, value) {
            manufacturerSelectBox.append('<option value="' + value + '">' + value + '</option>')
        });


        // Change manufacturer | load new data for product class
        manufacturerSelectBox.on("change", function () {
            // Reset value
            productClassSelectBox.val(null).change();
            productClassSelectBox.html('<option></option>');
            firmwareVersionSelectBox.val(null).change();
            firmwareVersionSelectBox.html('<option></option>');

            // Add data for product class select box
            if(listProductClass[manufacturerSelectBox.val()]) {
                $.each(listProductClass[manufacturerSelectBox.val()], function (key, value) {
                    productClassSelectBox.append('<option value="' + value + '">' + value + '</option>')
                });
            }
        });
        // Change product class | load new data for version
        productClassSelectBox.on("change", function () {
            // Reset value
            firmwareVersionSelectBox.val(null).change();
            firmwareVersionSelectBox.html('<option></option>');

            // Add data for product class select box
            if(manufacturerSelectBox.val() && productClassSelectBox.val()
                && listDeviceTypeId[manufacturerSelectBox.val()][productClassSelectBox.val()]) {

                var deviceTypeId = listDeviceTypeId[manufacturerSelectBox.val()][productClassSelectBox.val()];
                if(listVersion[deviceTypeId]) {
                    $.each(listVersion[deviceTypeId], function (key, value) {
                        firmwareVersionSelectBox.append('<option value="' + value + '">' + value + '</option>')
                    });
                }
            }

        });


        // Set selected first load
        var manufacturerParam = urlParams['Manufacturer'],
        productClassParam = urlParams['Product Class'],
        firmwareParam = urlParams['Firmware version'],
        onlineStateParam = urlParams['onlineState'];

        manufacturerSelectBox.val(manufacturerParam).change();
        if(productClassParam) {
            productClassSelectBox.html('<option value="' + productClassParam + '">' + productClassParam + '</option>')
            productClassSelectBox.val(productClassParam).change();
        }
        if(firmwareParam) {
            firmwareVersionSelectBox.html('<option value="' + firmwareParam + '">' + firmwareParam + '</option>')
            firmwareVersionSelectBox.val(firmwareParam).change();
        }
        if(onlineStateParam === 'Online') {
            $('#onlineStateOnline').parent().addClass('checked');
            $('#onlineStateOffline').parent().removeClass('checked');
        } else if(onlineStateParam === 'Offline') {
            $('#onlineStateOffline').parent().addClass('checked');
            $('#onlineStateOnline').parent().removeClass('checked');
        }

    });

});
