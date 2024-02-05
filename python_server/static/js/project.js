// //@ sourceURL=project.js
// $(function(){
//
// 	//给form表单添加submit事件
// 	$("form").submit(function(){
// 		return login();
// 	});
//
// });
var ipaddress = "192.168.96.177:5000" //192.168.96.177  127.0.0.1

function create(){

	$.ajax({
		url:"http://"+ipaddress+"/project/mkdir",
		type:"PUT",
		success:function(result){
			alert(1);
		},
		error:function(result){
			alert(2);
		}
	});
	
	return false;
}


$(document).ready(function() {
	$('#uploadForm').submit(function(event) {
		event.preventDefault(); // 阻止表单默认提交行为
		var formData = new FormData($(this)[0]);
		$.ajax({
			url: "http://"+ipaddress+"/project/upload",
			type: 'POST',
			data: formData,
			processData: false,
			contentType: false,
			success: function(result) {
				alert(1);
			},
			error: function(result) {
				alert(2);
			}
		});
	});
});

function runTask(){

	$.ajax({
		url:"http://"+ipaddress+"/project/runTask",
		type:"PUT",
		success:function(result){
			alert(1);
		},
		error:function(result){
			alert(2);
		}
	});

	return false;
}