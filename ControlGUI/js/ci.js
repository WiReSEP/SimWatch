// enable ace editor
var editor = ace.edit('editor');
editor.setTheme('ace/theme/tomorrow');
editor.session.setMode('ace/mode/json');
editor.$blockScrolling = Infinity;
editor.setReadOnly(true);
editor.setOption("minLines", 5);
editor.setOption("maxLines", 35);

// show that JavaScript works.
editor.setValue('"Choose a Profile to edit"');

// enable loadProfiles button
$('#loadProfiles').click(toggleProfiles);

// enable profile buttons
$('#profiles').on('click', '.profile', openProfile);

// enable save button
$('#save').click(saveProfile);

/**
 * callback function to toggle profile names
 */
function toggleProfiles(event) {
	event.preventDefault();
	if ($('#loadProfiles').hasClass('enabled')) {
		$('#loadProfiles').removeClass('enabled');
		$('#loadProfiles').addClass('disabled');
		hideProfiles(event);
	} else {
		$('#loadProfiles').removeClass('disabled');
		$('#loadProfiles').addClass('enabled');
		loadProfiles(event);
	}
}

/**
 * load and show the names of all profiles
 */
 function loadProfiles() {
	$('#profile_column').removeClass('hidden');
	$('#profiles').empty();
	$.ajax(
		'/dummy-api/',
		{
			type: 'get',
			data: {'action': 'list'},
			success: function(data, textStatus, jqXHR) {
				JSON.parse(data).forEach(
					function(element, index, array) {
						$('#profiles').append('<li><a href="' + element
							+ '" name="' + element
							+ '" class="profile">' + element
							+ '</a></li>')
					}
				);
			},
			error: function(jqXHR, textStatus, errorThrown) {
				console.log(errorThrown);
			}
		}
	);
}

/**
 * hide profile names
 */
 function hideProfiles() {
	$('#profile_column').addClass('hidden');
 }

/**
 * callback function to load and show a specific profile
 */
 function openProfile(event) {
	event.preventDefault();
	editor.setReadOnly(true);
	editor.setValue('"loading profile ' + event.target.name + '"');
	$.ajax(
		'/dummy-api/',
		{
			type: 'get',
			data: {'action': 'get', 'name': event.target.name, 'pass': 'secret'},
			success: function(data, textStatus, jqXHR) {
				if (data == 'pass') {
					var pass = prompt('enter pass');
					console.log('todo: try again with password');
				} else {
					editor.setValue(JSON.stringify(JSON.parse(data), null, "\t"));
					editor.setReadOnly(false);
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				console.log(errorThrown);
			}
		}
	);
}

/**
 * callback function to save the current profile
 */

function saveProfile(event) {
	event.preventDefault();
	try {
		var newProfile = JSON.stringify(JSON.parse(editor.getValue()));
	} catch (e) {
		alert(e);
		return;
	}
	$.ajax(
		'/dummy-api/',
		{
			type: 'get',
			data: {'action': 'save', 'profile': newProfile},
			success: function(data, textStatus, jqXHR) {
				alert('successfully saved');
			},
			error: function(jqXHR, textStatus, errorThrown) {
				console.log(errorThrown);
				alert(errorThrown);
			}
		}
	);
}
