$(function () {

    console.debug('START POLICY');

    // ACTIVE SIDEBAR
    var arrPathname = location.pathname.split('/');
    var rootPathname = '/' + arrPathname[1];
    $('.policy-sidebar').find('a[href="'+rootPathname+'"]').parent('li').addClass('active')

    // SHOW MESSAGE EXCEPTION
    var policyFileFormatException = ump.getUrlQueryValue('PolicyFileFormatException');
    if (policyFileFormatException && policyFileFormatException !== '') {
        var alertBox = '' +
            '<div class="alert alert-danger no-border">\n' +
            '    <button type="button" class="close" data-dismiss="alert"><span>×</span><span class="sr-only">Close</span></button>\n' +
            '    <span class="text-semibold">FILE FORMAT ERROR!</span> Change format file import and try submit again.\n' +
            '</div>';
        $('.panel-body').prepend(alertBox);
    }

});