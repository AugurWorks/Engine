package com.theconnman;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.model.*;
import org.scribe.utils.OAuthEncoder;

public class GitHubApi extends DefaultApi20 {
	private static final String AUTHORIZE_URL = "https://github.com/login/oauth/authorize?client_id=%s&client_secret=%s&redirect_uri=%s&response_type=scope";
	private static final String SCOPED_AUTHORIZE_URL = AUTHORIZE_URL + "&code=%s";

	@Override
	public Verb getAccessTokenVerb() {
		return Verb.GET;
	}

	@Override
	public String getAccessTokenEndpoint() {
		return "https://github.com/login/oauth/access_token?grant_type=authorization_code";
	}

	@Override
	public String getAuthorizationUrl(OAuthConfig config) {
		if (config.hasScope()) {
			System.out.println("Here");
			return String.format(SCOPED_AUTHORIZE_URL, config.getApiKey(), config.getApiSecret(), OAuthEncoder.encode(config.getCallback()), OAuthEncoder.encode(config.getScope()));
		} else {
			return String.format(AUTHORIZE_URL, config.getApiKey(), config.getApiSecret(), OAuthEncoder.encode(config.getCallback()));
		}
	}
}