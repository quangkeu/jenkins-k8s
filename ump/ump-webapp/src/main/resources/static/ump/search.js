$(document).ready(function() {

    $('#OUI').click(function() {
        $('#prefix').val('');
    });

    $('#Manufacturer').click(function() {
        $('#prefix').val('');
    });

    $('#Product Class').click(function() {
        $('#prefix').val('');
    });

    $(".removefromcart").on("click", function(e){
        e.stopPropagation();
    });


    // Default initialization
        $(".styled, .multiselect-container input").uniform({
            radioClass: 'choice'
        });

        // File input
        $(".file-styled").uniform({
            wrapperClass: 'bg-blue',
            fileButtonHtml: '<i class="icon-file-plus"></i>'
        });

    $(".control-success").uniform({
        radioClass: 'choice',
        wrapperClass: 'border-success-600 text-success-800'
    });

      // Danger
        $(".control-danger").uniform({
            radioClass: 'choice',
            wrapperClass: 'border-danger-600 text-danger-800'
        });

         //list of paramters in dom
    var paramsManufac = $('#searchManufac').attr('data-params-manufac');
    if(paramsManufac != undefined) {
        $('#Manufacturer').autocomplete({
              source: paramsManufac.split(",")
        });
    }

    var paramsOui = $('#searchOui').attr('data-params-oui');
    if(paramsOui != undefined) {
        $('#OUI').autocomplete({
            source: paramsOui.split(",")
        });
    }

    var paramsProductClass = $('#searchProductClass').attr('data-params-productClass');
    if(paramsProductClass != undefined) {
        $("[id='Product Class']").autocomplete({
            source: paramsProductClass.split(",")
        });
    }

    var paramsFirmwareVersion = $('#searchFirmwareVersion').attr('data-params-firmwareVersion');
    if(paramsFirmwareVersion != undefined) {
        $("[id='Firmware version']").autocomplete({
            source: paramsFirmwareVersion.split(",")
        });
    }

});
