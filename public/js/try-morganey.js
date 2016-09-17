const prompt = "λ> ";

$(document).ready(function() {
	
	function onValidate(line) {
		return true;
	}
	
	function onCommand(line, report) {
	    line = line.trim();

	    if (line.length == 0) {
	        return message("", true);
	    }

		function onResponse(response) {
			var messages = response.messages.map(function (x) {
				return message(x, response.error);
			});

			if (messages.length == 0) {
			    messages = [ message("", false) ];
			}
			
			messages.forEach(function (elem) {
				report(elem);
			});
		}
		
		$.ajax({
			type        : "POST",
			url         : "/evaluate",
			dataType    : "json",
			contentType : "application/json",
			data        : JSON.stringify({ "term": line }),
			success     : onResponse
		});
	}
	
	function message(text, error) {
		return [{
			"msg"       : text,
			"className" : error ? 
				"jquery-console-message-error" : 
				"jquery-console-message-value jquery-console-message-type"
		}];
	}
	
	function onAutocomplete(line) {
		function onResponse(response) {
			if (!response.error) {
				var prompt;
				if (response.messages.length == 1) {
					prompt = "";
				} else {
					prompt = line;
				}
				repl.showCompletion(prompt, response.messages);
			}
		}
		
		$.ajax({
			type        : "POST",
			url         : "/autocomplete",
			dataType    : "json",
			contentType : "application/json",
			data        : JSON.stringify({ "line": line }),
			success     : onResponse
		});
	}
	
	var replSettings = {
		promptLabel     : prompt,
		commandValidate : onValidate,
		commandHandle   : onCommand,
		welcomeMessage  : "Enter a Morganey term:",
		autofocus       : true,
		animateScroll   : true,
		promptHistory   : true,
		cols			: 80,
		completeIssuer  : onAutocomplete
	};

	var repl = $(".console").console(replSettings);
	
});

