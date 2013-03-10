$(function(){
    $("#getRandom").click(function(){
    $.getJSON('/api/random', function(data) {
        $("#wordFrom").val(data[0]);
        $("#wordTo").val(data[1]);
    });
    });
});
