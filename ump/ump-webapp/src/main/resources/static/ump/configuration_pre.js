$(function() {
    showTag($('#first-tag-id').text());
});

function showTag(value)
{
        var id = $('#deviceId').text();
        showLoading('configuration-data');
        $("#tag-content").load("/getTagPresentation?tagId=" + value+"&deviceId="+id, function(responseText, textStatus) {
          if (textStatus === "error") {
              return "false";
          }
      });
      return false;
}