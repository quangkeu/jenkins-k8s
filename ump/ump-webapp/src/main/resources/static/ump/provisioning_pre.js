$(function() {
    showTag($('#first-tag-id').text());
});

function showTag(value)
{

          $("#provisioning-data").block({
                    	    message: '<i class="icon-spinner4 spinner"></i>',
                    	    timeout: 120000, //unblock after 1 minutes
                    	    overlayCSS: {
                    	        backgroundColor: '#fff',
                    	        opacity: 0.8,
                    	        cursor: 'wait'
                    	    },
                    	    css: {
                    	        border: 0,
                    	        padding: 0,
                    	        backgroundColor: 'transparent'
                    	    }
                    	});
        var deviceTypeVersionId = $('#deviceTypeVersionId').text();
        $("#tag-content").load("/provisioning/getData?tagId=" + value +"&deviceTypeVersionId="+deviceTypeVersionId, function(responseText, textStatus) {
          if (textStatus === "error") {
              return "false";
          }
      });
      return false;
}