package net.kaleidos.grails.plugin.security.stateless.token

import net.kaleidos.grails.plugin.security.stateless.CryptoService

import grails.test.mixin.TestFor
import spock.lang.Specification

import net.kaleidos.grails.plugin.security.stateless.provider.UserSaltProvider

class LegacyStatelessTokenProviderSpec extends Specification {
    def tokenProvider

    def setup() {
        tokenProvider = new LegacyStatelessTokenProvider()
        tokenProvider.cryptoService = new CryptoService()
        tokenProvider.cryptoService.init 'secret'
    }

    void "generate a token and then extract it"() {
        setup:
            def username = 'palba'
            def token = tokenProvider.generateToken(username, salt)

        when:
            def data = tokenProvider.validateAndExtractToken(token)

        then:
            data.username == 'palba'
            data.extradata == [:]
            data.issued_at != null
            data.salt == salt

        where:
            salt = "salt"
    }

    void "generate a token with extra data"() {
        setup:
            def username = 'palba'
            def extraData = ['token1':'AAA', 'token2':'BBB']
            def token = tokenProvider.generateToken(username, salt, extraData)

        when:
            def data = tokenProvider.validateAndExtractToken(token)

        then:
            data.username == 'palba'
            data.extradata['token1'] == 'AAA'
            data.extradata['token2'] == 'BBB'
            data.issued_at != null
            data.salt == salt

        where:
            salt = "salt"
    }

    void "Can't extract token with an invalid salt"() {
        setup:
            def username = 'palba'
            def extraData = ['token1':'AAA', 'token2':'BBB']
            def token = tokenProvider.generateToken(username, salt1, extraData)

        when:
            def data = tokenProvider.validateAndExtractToken(token, salt2)

        then:
            thrown(RuntimeException)

        where:
            salt1 = "salt"
            salt2 = "salt2"
    }

    void "Try to extract invalid token"() {
        setup:
            def token = "XXXXXXXXXXXX"

        when:
            def data = tokenProvider.validateAndExtractToken(token)

        then:
            thrown(RuntimeException)
    }
}