$(function () {

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

    //<editor-fold desc="DELETE POLICY">
    $('.deletes-btn').click(function () {
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
                location.reload();
            }
        });
    }
    //</editor-fold>
    
});