$(function () {

    console.debug('START POLICY OPERATION');

    // DECLARE VARIABLE
    var parameters = [];
    var parametersByPath = [];
    var parametersSelected = [];
    var parametersAutocomplete = [];

    var actionOptions = [];
    var actionOptionsMaster = {
        backup: 'Backup',
        restore: 'Restore',
        reboot: 'Reboot',
        factoryReset: 'Factory Reset',
        updateFirmware: 'Update Firmware',
        downloadVendorConfigurationFile: 'Download Vendor Configuration File'
    };

    var fileTypeOptions = [];
    var fileTypeOptionsMaster = {
        backup: ['Full device configuration', 'Vendor configuration file, Firmware Image'],
        restore: ['Full device configuration', 'Vendor configuration file', 'Firmware Image'],
        reboot: {},
        factoryReset: {},
        updateFirmware: {},
        downloadVendorConfigurationFile: {}
    };

    // DECLARE HTML VARIABLE
    var policy_operation_form = $('.policy-operation-form');
    var policy_operation_validator;

    // FIRST LOAD PAGE
    firstLoadPage();

    //<editor-fold desc="SET DATA TO INPUT IN FIRST LOAD">
    var actionSelected = $('.action-input').attr('data-actionName');
    $('.action-input').val(actionSelected).change();
    if (actionSelected === 'backup' || actionSelected === 'restore') {
        generateFileTypeInput(actionSelected);
        $('.file-type-input').val($('.file-type-input').attr('data-fileType')).change();

    } else if (actionSelected === 'updateFirmware') {
        generateFileFirmwareIdInput(actionSelected);
        $('.form-group-file-firmware-id').find('.file-id-input').val($('.form-group-file-firmware-id').find('.file-id-input').attr('data-fileId')).change();

    } else if (actionSelected === 'downloadVendorConfigurationFile') {
        generateFileVendorIdInput(actionSelected);
        $('.form-group-file-vendor-id').find('.file-id-input').val($('.form-group-file-vendor-id').find('.file-id-input').attr('data-fileId')).change();

    } else if (actionSelected === 'reboot' || actionSelected === 'factoryReset') {
        $('.form-group-file-firmware-id').hide();
        $('.form-group-file-vendor-id').hide();
        $('.form-group-file-type').hide();
    }
    $('input[type=file]').removeAttr('required');

    //</editor-fold>

    // EVENT IN PAGE
    $('input[name=selectedTargetedDevices]').change(function () {
        generateValidateSelectedTargetedDevices($(this).val())
    });

    $('input[name=schedule]').change(function () {
        generateValidateSchedule($(this).val())
    });

    $('.action-input').on('select2:select', function (e) {
        var actionSelected = e.params.data.id;

        if (actionSelected === 'backup' || actionSelected === 'restore') {
            generateFileTypeInput(actionSelected)

        }  else if (actionSelected === 'updateFirmware') {
            generateFileFirmwareIdInput(actionSelected)

        } else if (actionSelected === 'downloadVendorConfigurationFile') {
            generateFileVendorIdInput(actionSelected)

        } else if (actionSelected === 'reboot' || actionSelected === 'factoryReset') {
            $('.form-group-file-firmware-id').hide();
            $('.form-group-file-vendor-id').hide();
            $('.form-group-file-type').hide();
        }
    });

    function generateFileTypeInput(actionSelected) {
        fileTypeOptions = [{id:'', text:''}];
        $.each(fileTypeOptionsMaster[actionSelected], function (index, value) {
            fileTypeOptions.push({ id: value, text: value})
        });
        $('.file-type-input').empty().select2('destroy').select2({
            placeholder: '- Select file type -',
            data: fileTypeOptions,
            disabled: false
        });

        $('.form-group-file-type').show();
        $('.form-group-file-firmware-id').hide();
        $('.form-group-file-vendor-id').hide();
        $('.file-id-input').val('').change();
    }

    function generateFileFirmwareIdInput(actionSelected) {
        $('.form-group-file-firmware-id').show();
        $('.form-group-file-firmware-id').find('select').removeAttr('disabled');
        $('.form-group-file-vendor-id').hide();
        $('.form-group-file-vendor-id').find('select').attr('disabled', 'true');
        $('.form-group-file-type').hide();
    }

    function generateFileVendorIdInput(actionSelected) {
        $('.form-group-file-vendor-id').show();
        $('.form-group-file-vendor-id').find('select').removeAttr('disabled');
        $('.form-group-file-firmware-id').hide();
        $('.form-group-file-firmware-id').find('select').attr('disabled', 'true');
        $('.form-group-file-type').hide();
    }


    //<editor-fold desc="FORM PRIVATE FUNCTION">
    function  firstLoadPage() {
        initLibrary();
        generateValidateSelectedTargetedDevices($('input[name=selectedTargetedDevices][checked=checked]').val());
        generateValidateSchedule($('input[name=schedule][checked=checked]').val());
    }
    
    function initLibrary() {

        $("#anytime-both").AnyTime_picker({
            format: "%d-%m-%Y %H:%i"
        });

        $(".file-styled").uniform({
            fileButtonClass: 'action btn btn-default'
        });

        $('.device-group-select2').select2({
            placeholder: '- Select a group device -'
        });

        actionOptions = [{id:'', text:''}];
        $.each(actionOptionsMaster, function (index, value) {
            actionOptions.push({ id: index, text: value})
        });
        $('.action-input').select2({
            placeholder: '- Select action -',
            data: actionOptions
        });

        fileTypeOptions = [{id:'', text:''}];
        $('.file-type-input').select2({
            placeholder: '- Select action -',
            data: fileTypeOptions,
            disabled: true
        });

        $('.file-id-input').select2({
            placeholder: '- Select file -'
        });

        policy_operation_validator =  policy_operation_form.validate(ump.validator);

    }

    if(policy_configuration_form.find('input[name="radioLimited"]').val() === '0') {
        policy_configuration_form.find('input[name="limited"]').hide().val('').removeAttr('required');
        policy_operation_validator.resetForm();
    } else {
        policy_configuration_form.find('input[name="limited"]').show().attr('required', 'required');
    }


    policy_operation_form.find('input[name="radioLimited"]').change(function () {
        if($(this).val() === '0') {
            policy_operation_form.find('input[name="limited"]').hide().val('');
        } else {
            policy_operation_form.find('input[name="limited"]').show();
        }
    });

    function generateValidateSelectedTargetedDevices(value) {
        policy_operation_validator.resetForm();

        if(value === 'byFilters') {
            $('.byFilters').each(function () {
                $(this).removeAttr('disabled').attr('required', 'required');
            });
            $('.byExternalFile').each(function () {
                $(this).removeAttr('required').attr('disabled', 'disabled');
            })

        } else if(value === 'byExternalFile') {
            $('.byExternalFile').each(function () {
                $(this).removeAttr('disabled').attr('required', 'required');
            });
            $('.byFilters').each(function () {
                $(this).removeAttr('required').attr('disabled', 'disabled');
            })

        } else {
            $('.byFilters').each(function () {
                $(this).removeAttr('required').attr('disabled', 'disabled');
            });
            $('.byExternalFile').each(function () {
                $(this).removeAttr('required').attr('disabled', 'disabled');
            });
        }
    }

    function generateValidateSchedule(value) {
        policy_operation_validator.resetForm();

        if(value === 'startAt') {
            $('.startAt').each(function () {
                $(this).removeAttr('disabled').attr('required', 'required');
            });
            $('.unrequired').each(function () {
                $(this).removeAttr('required');
            });
            $('.immediately').each(function () {
                $(this).removeAttr('required').attr('disabled', 'disabled');
            })

        } else if(value === 'immediately') {
            $('.immediately').each(function () {
                $(this).removeAttr('disabled').attr('required', 'required');
            });

            $('.startAt').each(function () {
                $(this).removeAttr('required').attr('disabled', 'disabled');
            })

        } else {
            $('.startAt').each(function () {
                $(this).removeAttr('required').attr('disabled', 'disabled');
            });

            $('.immediately').each(function () {
                $(this).removeAttr('required').attr('disabled', 'disabled');
            });
        }
    }

    // Validate number
    $('.validateInteger').on('keyup change paste', function () {
        var alNumRegexPhone = /[^0-9]/g; //only numbers
        if (alNumRegexPhone.test($(this).val())) {
            $(this).parent().find('.text-error-integer').remove();
            $(this).parent().append('<span class="text-error-integer text-danger">Input invalid, please enter an integer.</span>');
            $('.btnSave').attr('disabled', true);
        } else {
            $(this).parent().find('.text-error-integer').remove();
            if (policy_operation_form.find('.text-error-integer').length === 0) {
                $('.btnSave').removeAttr('disabled');
            }
        }

    })

    //</editor-fold>

});