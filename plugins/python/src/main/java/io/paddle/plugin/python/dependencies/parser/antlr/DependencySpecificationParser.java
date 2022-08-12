package io.paddle.plugin.python.dependencies.parser.antlr;

// Generated from DependencySpecification.g4 by ANTLR 4.10.1

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class DependencySpecificationParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.10.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		WSP=18, LETTER=19, DIGIT=20, ENV_VAR=21, VERSION_CMP=22, IDENT_END=23, 
		IDENT=24, PYTHON_STR_C=25, DQUOTE=26, SQUOTE=27, PYTHON_STR=28;
	public static final int
		RULE_version = 0, RULE_versionCmp = 1, RULE_versionOne = 2, RULE_versionMany = 3, 
		RULE_versionspec = 4, RULE_markerOp = 5, RULE_markerVar = 6, RULE_markerExpr = 7, 
		RULE_markerAnd = 8, RULE_markerOr = 9, RULE_marker = 10, RULE_quotedMarker = 11, 
		RULE_name = 12, RULE_extrasList = 13, RULE_extras = 14, RULE_nameReq = 15, 
		RULE_specification = 16;
	private static String[] makeRuleNames() {
		return new String[] {
			"version", "versionCmp", "versionOne", "versionMany", "versionspec", 
			"markerOp", "markerVar", "markerExpr", "markerAnd", "markerOr", "marker", 
			"quotedMarker", "name", "extrasList", "extras", "nameReq", "specification"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'-'", "'_'", "'.'", "'*'", "'+'", "'!'", "','", "'('", "')'", 
			"'in'", "'not'", "'and'", "'or'", "';'", "'['", "']'", "'\\n'", null, 
			null, null, null, null, null, null, null, "'\"'", "'''"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, "WSP", "LETTER", "DIGIT", "ENV_VAR", 
			"VERSION_CMP", "IDENT_END", "IDENT", "PYTHON_STR_C", "DQUOTE", "SQUOTE", 
			"PYTHON_STR"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "DependencySpecification.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public DependencySpecificationParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class VersionContext extends ParserRuleContext {
		public List<TerminalNode> WSP() { return getTokens(DependencySpecificationParser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(DependencySpecificationParser.WSP, i);
		}
		public List<TerminalNode> IDENT() { return getTokens(DependencySpecificationParser.IDENT); }
		public TerminalNode IDENT(int i) {
			return getToken(DependencySpecificationParser.IDENT, i);
		}
		public List<TerminalNode> LETTER() { return getTokens(DependencySpecificationParser.LETTER); }
		public TerminalNode LETTER(int i) {
			return getToken(DependencySpecificationParser.LETTER, i);
		}
		public List<TerminalNode> DIGIT() { return getTokens(DependencySpecificationParser.DIGIT); }
		public TerminalNode DIGIT(int i) {
			return getToken(DependencySpecificationParser.DIGIT, i);
		}
		public VersionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_version; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).enterVersion(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).exitVersion(this);
		}
	}

	public final VersionContext version() throws RecognitionException {
		VersionContext _localctx = new VersionContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_version);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(37);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(34);
				match(WSP);
				}
				}
				setState(39);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(41); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(40);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << LETTER) | (1L << DIGIT) | (1L << IDENT))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				setState(43); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << LETTER) | (1L << DIGIT) | (1L << IDENT))) != 0) );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VersionCmpContext extends ParserRuleContext {
		public TerminalNode VERSION_CMP() { return getToken(DependencySpecificationParser.VERSION_CMP, 0); }
		public VersionCmpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_versionCmp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).enterVersionCmp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).exitVersionCmp(this);
		}
	}

	public final VersionCmpContext versionCmp() throws RecognitionException {
		VersionCmpContext _localctx = new VersionCmpContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_versionCmp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(45);
			match(VERSION_CMP);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VersionOneContext extends ParserRuleContext {
		public VersionCmpContext versionCmp() {
			return getRuleContext(VersionCmpContext.class,0);
		}
		public VersionContext version() {
			return getRuleContext(VersionContext.class,0);
		}
		public List<TerminalNode> WSP() { return getTokens(DependencySpecificationParser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(DependencySpecificationParser.WSP, i);
		}
		public VersionOneContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_versionOne; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).enterVersionOne(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).exitVersionOne(this);
		}
	}

	public final VersionOneContext versionOne() throws RecognitionException {
		VersionOneContext _localctx = new VersionOneContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_versionOne);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(47);
			versionCmp();
			setState(48);
			version();
			setState(52);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(49);
					match(WSP);
					}
					} 
				}
				setState(54);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VersionManyContext extends ParserRuleContext {
		public List<VersionOneContext> versionOne() {
			return getRuleContexts(VersionOneContext.class);
		}
		public VersionOneContext versionOne(int i) {
			return getRuleContext(VersionOneContext.class,i);
		}
		public List<TerminalNode> WSP() { return getTokens(DependencySpecificationParser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(DependencySpecificationParser.WSP, i);
		}
		public VersionManyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_versionMany; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).enterVersionMany(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).exitVersionMany(this);
		}
	}

	public final VersionManyContext versionMany() throws RecognitionException {
		VersionManyContext _localctx = new VersionManyContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_versionMany);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(55);
			versionOne();
			setState(66);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(59);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==WSP) {
						{
						{
						setState(56);
						match(WSP);
						}
						}
						setState(61);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(62);
					match(T__6);
					setState(63);
					versionOne();
					}
					} 
				}
				setState(68);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VersionspecContext extends ParserRuleContext {
		public VersionManyContext versionMany() {
			return getRuleContext(VersionManyContext.class,0);
		}
		public VersionspecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_versionspec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).enterVersionspec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).exitVersionspec(this);
		}
	}

	public final VersionspecContext versionspec() throws RecognitionException {
		VersionspecContext _localctx = new VersionspecContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_versionspec);
		try {
			setState(74);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__7:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(69);
				match(T__7);
				setState(70);
				versionMany();
				setState(71);
				match(T__8);
				}
				}
				break;
			case VERSION_CMP:
				enterOuterAlt(_localctx, 2);
				{
				setState(73);
				versionMany();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MarkerOpContext extends ParserRuleContext {
		public TerminalNode VERSION_CMP() { return getToken(DependencySpecificationParser.VERSION_CMP, 0); }
		public List<TerminalNode> WSP() { return getTokens(DependencySpecificationParser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(DependencySpecificationParser.WSP, i);
		}
		public MarkerOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_markerOp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).enterMarkerOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).exitMarkerOp(this);
		}
	}

	public final MarkerOpContext markerOp() throws RecognitionException {
		MarkerOpContext _localctx = new MarkerOpContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_markerOp);
		int _la;
		try {
			int _alt;
			setState(105);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(76);
				match(VERSION_CMP);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(78); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(77);
					match(WSP);
					}
					}
					setState(80); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==WSP );
				setState(82);
				match(T__9);
				setState(84); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(83);
						match(WSP);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(86); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				{
				setState(89); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(88);
					match(WSP);
					}
					}
					setState(91); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==WSP );
				setState(93);
				match(T__10);
				setState(95); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(94);
					match(WSP);
					}
					}
					setState(97); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==WSP );
				setState(99);
				match(T__9);
				setState(101); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(100);
						match(WSP);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(103); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MarkerVarContext extends ParserRuleContext {
		public TerminalNode ENV_VAR() { return getToken(DependencySpecificationParser.ENV_VAR, 0); }
		public TerminalNode PYTHON_STR() { return getToken(DependencySpecificationParser.PYTHON_STR, 0); }
		public List<TerminalNode> WSP() { return getTokens(DependencySpecificationParser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(DependencySpecificationParser.WSP, i);
		}
		public MarkerVarContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_markerVar; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).enterMarkerVar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).exitMarkerVar(this);
		}
	}

	public final MarkerVarContext markerVar() throws RecognitionException {
		MarkerVarContext _localctx = new MarkerVarContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_markerVar);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(110);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(107);
				match(WSP);
				}
				}
				setState(112);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(113);
			_la = _input.LA(1);
			if ( !(_la==ENV_VAR || _la==PYTHON_STR) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MarkerExprContext extends ParserRuleContext {
		public List<MarkerVarContext> markerVar() {
			return getRuleContexts(MarkerVarContext.class);
		}
		public MarkerVarContext markerVar(int i) {
			return getRuleContext(MarkerVarContext.class,i);
		}
		public MarkerOpContext markerOp() {
			return getRuleContext(MarkerOpContext.class,0);
		}
		public MarkerContext marker() {
			return getRuleContext(MarkerContext.class,0);
		}
		public List<TerminalNode> WSP() { return getTokens(DependencySpecificationParser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(DependencySpecificationParser.WSP, i);
		}
		public MarkerExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_markerExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).enterMarkerExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).exitMarkerExpr(this);
		}
	}

	public final MarkerExprContext markerExpr() throws RecognitionException {
		MarkerExprContext _localctx = new MarkerExprContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_markerExpr);
		int _la;
		try {
			setState(135);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(115);
				markerVar();
				setState(116);
				markerOp();
				setState(117);
				markerVar();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(122);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WSP) {
					{
					{
					setState(119);
					match(WSP);
					}
					}
					setState(124);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(125);
				match(T__7);
				setState(126);
				marker();
				setState(130);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WSP) {
					{
					{
					setState(127);
					match(WSP);
					}
					}
					setState(132);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(133);
				match(T__8);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MarkerAndContext extends ParserRuleContext {
		public List<MarkerExprContext> markerExpr() {
			return getRuleContexts(MarkerExprContext.class);
		}
		public MarkerExprContext markerExpr(int i) {
			return getRuleContext(MarkerExprContext.class,i);
		}
		public List<TerminalNode> WSP() { return getTokens(DependencySpecificationParser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(DependencySpecificationParser.WSP, i);
		}
		public MarkerAndContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_markerAnd; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).enterMarkerAnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).exitMarkerAnd(this);
		}
	}

	public final MarkerAndContext markerAnd() throws RecognitionException {
		MarkerAndContext _localctx = new MarkerAndContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_markerAnd);
		int _la;
		try {
			setState(148);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(137);
				markerExpr();
				setState(141);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WSP) {
					{
					{
					setState(138);
					match(WSP);
					}
					}
					setState(143);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(144);
				match(T__11);
				setState(145);
				markerExpr();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(147);
				markerExpr();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MarkerOrContext extends ParserRuleContext {
		public List<MarkerAndContext> markerAnd() {
			return getRuleContexts(MarkerAndContext.class);
		}
		public MarkerAndContext markerAnd(int i) {
			return getRuleContext(MarkerAndContext.class,i);
		}
		public List<TerminalNode> WSP() { return getTokens(DependencySpecificationParser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(DependencySpecificationParser.WSP, i);
		}
		public MarkerOrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_markerOr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).enterMarkerOr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).exitMarkerOr(this);
		}
	}

	public final MarkerOrContext markerOr() throws RecognitionException {
		MarkerOrContext _localctx = new MarkerOrContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_markerOr);
		int _la;
		try {
			setState(161);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(150);
				markerAnd();
				setState(154);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WSP) {
					{
					{
					setState(151);
					match(WSP);
					}
					}
					setState(156);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(157);
				match(T__12);
				setState(158);
				markerAnd();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(160);
				markerAnd();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MarkerContext extends ParserRuleContext {
		public MarkerOrContext markerOr() {
			return getRuleContext(MarkerOrContext.class,0);
		}
		public MarkerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_marker; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).enterMarker(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).exitMarker(this);
		}
	}

	public final MarkerContext marker() throws RecognitionException {
		MarkerContext _localctx = new MarkerContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_marker);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(163);
			markerOr();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QuotedMarkerContext extends ParserRuleContext {
		public MarkerContext marker() {
			return getRuleContext(MarkerContext.class,0);
		}
		public List<TerminalNode> WSP() { return getTokens(DependencySpecificationParser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(DependencySpecificationParser.WSP, i);
		}
		public QuotedMarkerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_quotedMarker; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).enterQuotedMarker(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).exitQuotedMarker(this);
		}
	}

	public final QuotedMarkerContext quotedMarker() throws RecognitionException {
		QuotedMarkerContext _localctx = new QuotedMarkerContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_quotedMarker);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(165);
			match(T__13);
			setState(169);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(166);
					match(WSP);
					}
					} 
				}
				setState(171);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			}
			setState(172);
			marker();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NameContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(DependencySpecificationParser.IDENT, 0); }
		public NameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).enterName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).exitName(this);
		}
	}

	public final NameContext name() throws RecognitionException {
		NameContext _localctx = new NameContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(174);
			match(IDENT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExtrasListContext extends ParserRuleContext {
		public List<NameContext> name() {
			return getRuleContexts(NameContext.class);
		}
		public NameContext name(int i) {
			return getRuleContext(NameContext.class,i);
		}
		public List<TerminalNode> WSP() { return getTokens(DependencySpecificationParser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(DependencySpecificationParser.WSP, i);
		}
		public ExtrasListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extrasList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).enterExtrasList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).exitExtrasList(this);
		}
	}

	public final ExtrasListContext extrasList() throws RecognitionException {
		ExtrasListContext _localctx = new ExtrasListContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_extrasList);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(176);
			name();
			setState(193);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(180);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==WSP) {
						{
						{
						setState(177);
						match(WSP);
						}
						}
						setState(182);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(183);
					match(T__6);
					setState(187);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==WSP) {
						{
						{
						setState(184);
						match(WSP);
						}
						}
						setState(189);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(190);
					name();
					}
					} 
				}
				setState(195);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExtrasContext extends ParserRuleContext {
		public List<TerminalNode> WSP() { return getTokens(DependencySpecificationParser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(DependencySpecificationParser.WSP, i);
		}
		public ExtrasListContext extrasList() {
			return getRuleContext(ExtrasListContext.class,0);
		}
		public ExtrasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extras; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).enterExtras(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).exitExtras(this);
		}
	}

	public final ExtrasContext extras() throws RecognitionException {
		ExtrasContext _localctx = new ExtrasContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_extras);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(196);
			match(T__14);
			setState(200);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,24,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(197);
					match(WSP);
					}
					} 
				}
				setState(202);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,24,_ctx);
			}
			setState(204);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENT) {
				{
				setState(203);
				extrasList();
				}
			}

			setState(209);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(206);
				match(WSP);
				}
				}
				setState(211);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(212);
			match(T__15);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NameReqContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public List<TerminalNode> WSP() { return getTokens(DependencySpecificationParser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(DependencySpecificationParser.WSP, i);
		}
		public ExtrasContext extras() {
			return getRuleContext(ExtrasContext.class,0);
		}
		public VersionspecContext versionspec() {
			return getRuleContext(VersionspecContext.class,0);
		}
		public QuotedMarkerContext quotedMarker() {
			return getRuleContext(QuotedMarkerContext.class,0);
		}
		public NameReqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nameReq; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).enterNameReq(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).exitNameReq(this);
		}
	}

	public final NameReqContext nameReq() throws RecognitionException {
		NameReqContext _localctx = new NameReqContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_nameReq);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(214);
			name();
			setState(218);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,27,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(215);
					match(WSP);
					}
					} 
				}
				setState(220);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,27,_ctx);
			}
			setState(222);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__14) {
				{
				setState(221);
				extras();
				}
			}

			setState(227);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(224);
					match(WSP);
					}
					} 
				}
				setState(229);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
			}
			setState(231);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__7 || _la==VERSION_CMP) {
				{
				setState(230);
				versionspec();
				}
			}

			setState(236);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,31,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(233);
					match(WSP);
					}
					} 
				}
				setState(238);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,31,_ctx);
			}
			setState(240);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__13) {
				{
				setState(239);
				quotedMarker();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SpecificationContext extends ParserRuleContext {
		public NameReqContext nameReq() {
			return getRuleContext(NameReqContext.class,0);
		}
		public List<TerminalNode> WSP() { return getTokens(DependencySpecificationParser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(DependencySpecificationParser.WSP, i);
		}
		public SpecificationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_specification; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).enterSpecification(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DependencySpecificationListener ) ((DependencySpecificationListener)listener).exitSpecification(this);
		}
	}

	public final SpecificationContext specification() throws RecognitionException {
		SpecificationContext _localctx = new SpecificationContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_specification);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(245);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(242);
				match(WSP);
				}
				}
				setState(247);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			{
			setState(248);
			nameReq();
			}
			setState(252);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(249);
				match(WSP);
				}
				}
				setState(254);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(258);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__16) {
				{
				{
				setState(255);
				match(T__16);
				}
				}
				setState(260);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u001c\u0106\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007"+
		"\u000f\u0002\u0010\u0007\u0010\u0001\u0000\u0005\u0000$\b\u0000\n\u0000"+
		"\f\u0000\'\t\u0000\u0001\u0000\u0004\u0000*\b\u0000\u000b\u0000\f\u0000"+
		"+\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0005\u0002"+
		"3\b\u0002\n\u0002\f\u00026\t\u0002\u0001\u0003\u0001\u0003\u0005\u0003"+
		":\b\u0003\n\u0003\f\u0003=\t\u0003\u0001\u0003\u0001\u0003\u0005\u0003"+
		"A\b\u0003\n\u0003\f\u0003D\t\u0003\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0003\u0004K\b\u0004\u0001\u0005\u0001\u0005"+
		"\u0004\u0005O\b\u0005\u000b\u0005\f\u0005P\u0001\u0005\u0001\u0005\u0004"+
		"\u0005U\b\u0005\u000b\u0005\f\u0005V\u0001\u0005\u0004\u0005Z\b\u0005"+
		"\u000b\u0005\f\u0005[\u0001\u0005\u0001\u0005\u0004\u0005`\b\u0005\u000b"+
		"\u0005\f\u0005a\u0001\u0005\u0001\u0005\u0004\u0005f\b\u0005\u000b\u0005"+
		"\f\u0005g\u0003\u0005j\b\u0005\u0001\u0006\u0005\u0006m\b\u0006\n\u0006"+
		"\f\u0006p\t\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0005\u0007y\b\u0007\n\u0007\f\u0007|\t"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0005\u0007\u0081\b\u0007\n"+
		"\u0007\f\u0007\u0084\t\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0088"+
		"\b\u0007\u0001\b\u0001\b\u0005\b\u008c\b\b\n\b\f\b\u008f\t\b\u0001\b\u0001"+
		"\b\u0001\b\u0001\b\u0003\b\u0095\b\b\u0001\t\u0001\t\u0005\t\u0099\b\t"+
		"\n\t\f\t\u009c\t\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u00a2\b\t\u0001"+
		"\n\u0001\n\u0001\u000b\u0001\u000b\u0005\u000b\u00a8\b\u000b\n\u000b\f"+
		"\u000b\u00ab\t\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\r\u0001"+
		"\r\u0005\r\u00b3\b\r\n\r\f\r\u00b6\t\r\u0001\r\u0001\r\u0005\r\u00ba\b"+
		"\r\n\r\f\r\u00bd\t\r\u0001\r\u0005\r\u00c0\b\r\n\r\f\r\u00c3\t\r\u0001"+
		"\u000e\u0001\u000e\u0005\u000e\u00c7\b\u000e\n\u000e\f\u000e\u00ca\t\u000e"+
		"\u0001\u000e\u0003\u000e\u00cd\b\u000e\u0001\u000e\u0005\u000e\u00d0\b"+
		"\u000e\n\u000e\f\u000e\u00d3\t\u000e\u0001\u000e\u0001\u000e\u0001\u000f"+
		"\u0001\u000f\u0005\u000f\u00d9\b\u000f\n\u000f\f\u000f\u00dc\t\u000f\u0001"+
		"\u000f\u0003\u000f\u00df\b\u000f\u0001\u000f\u0005\u000f\u00e2\b\u000f"+
		"\n\u000f\f\u000f\u00e5\t\u000f\u0001\u000f\u0003\u000f\u00e8\b\u000f\u0001"+
		"\u000f\u0005\u000f\u00eb\b\u000f\n\u000f\f\u000f\u00ee\t\u000f\u0001\u000f"+
		"\u0003\u000f\u00f1\b\u000f\u0001\u0010\u0005\u0010\u00f4\b\u0010\n\u0010"+
		"\f\u0010\u00f7\t\u0010\u0001\u0010\u0001\u0010\u0005\u0010\u00fb\b\u0010"+
		"\n\u0010\f\u0010\u00fe\t\u0010\u0001\u0010\u0005\u0010\u0101\b\u0010\n"+
		"\u0010\f\u0010\u0104\t\u0010\u0001\u0010\u0000\u0000\u0011\u0000\u0002"+
		"\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e"+
		" \u0000\u0002\u0003\u0000\u0001\u0006\u0013\u0014\u0018\u0018\u0002\u0000"+
		"\u0015\u0015\u001c\u001c\u0119\u0000%\u0001\u0000\u0000\u0000\u0002-\u0001"+
		"\u0000\u0000\u0000\u0004/\u0001\u0000\u0000\u0000\u00067\u0001\u0000\u0000"+
		"\u0000\bJ\u0001\u0000\u0000\u0000\ni\u0001\u0000\u0000\u0000\fn\u0001"+
		"\u0000\u0000\u0000\u000e\u0087\u0001\u0000\u0000\u0000\u0010\u0094\u0001"+
		"\u0000\u0000\u0000\u0012\u00a1\u0001\u0000\u0000\u0000\u0014\u00a3\u0001"+
		"\u0000\u0000\u0000\u0016\u00a5\u0001\u0000\u0000\u0000\u0018\u00ae\u0001"+
		"\u0000\u0000\u0000\u001a\u00b0\u0001\u0000\u0000\u0000\u001c\u00c4\u0001"+
		"\u0000\u0000\u0000\u001e\u00d6\u0001\u0000\u0000\u0000 \u00f5\u0001\u0000"+
		"\u0000\u0000\"$\u0005\u0012\u0000\u0000#\"\u0001\u0000\u0000\u0000$\'"+
		"\u0001\u0000\u0000\u0000%#\u0001\u0000\u0000\u0000%&\u0001\u0000\u0000"+
		"\u0000&)\u0001\u0000\u0000\u0000\'%\u0001\u0000\u0000\u0000(*\u0007\u0000"+
		"\u0000\u0000)(\u0001\u0000\u0000\u0000*+\u0001\u0000\u0000\u0000+)\u0001"+
		"\u0000\u0000\u0000+,\u0001\u0000\u0000\u0000,\u0001\u0001\u0000\u0000"+
		"\u0000-.\u0005\u0016\u0000\u0000.\u0003\u0001\u0000\u0000\u0000/0\u0003"+
		"\u0002\u0001\u000004\u0003\u0000\u0000\u000013\u0005\u0012\u0000\u0000"+
		"21\u0001\u0000\u0000\u000036\u0001\u0000\u0000\u000042\u0001\u0000\u0000"+
		"\u000045\u0001\u0000\u0000\u00005\u0005\u0001\u0000\u0000\u000064\u0001"+
		"\u0000\u0000\u00007B\u0003\u0004\u0002\u00008:\u0005\u0012\u0000\u0000"+
		"98\u0001\u0000\u0000\u0000:=\u0001\u0000\u0000\u0000;9\u0001\u0000\u0000"+
		"\u0000;<\u0001\u0000\u0000\u0000<>\u0001\u0000\u0000\u0000=;\u0001\u0000"+
		"\u0000\u0000>?\u0005\u0007\u0000\u0000?A\u0003\u0004\u0002\u0000@;\u0001"+
		"\u0000\u0000\u0000AD\u0001\u0000\u0000\u0000B@\u0001\u0000\u0000\u0000"+
		"BC\u0001\u0000\u0000\u0000C\u0007\u0001\u0000\u0000\u0000DB\u0001\u0000"+
		"\u0000\u0000EF\u0005\b\u0000\u0000FG\u0003\u0006\u0003\u0000GH\u0005\t"+
		"\u0000\u0000HK\u0001\u0000\u0000\u0000IK\u0003\u0006\u0003\u0000JE\u0001"+
		"\u0000\u0000\u0000JI\u0001\u0000\u0000\u0000K\t\u0001\u0000\u0000\u0000"+
		"Lj\u0005\u0016\u0000\u0000MO\u0005\u0012\u0000\u0000NM\u0001\u0000\u0000"+
		"\u0000OP\u0001\u0000\u0000\u0000PN\u0001\u0000\u0000\u0000PQ\u0001\u0000"+
		"\u0000\u0000QR\u0001\u0000\u0000\u0000RT\u0005\n\u0000\u0000SU\u0005\u0012"+
		"\u0000\u0000TS\u0001\u0000\u0000\u0000UV\u0001\u0000\u0000\u0000VT\u0001"+
		"\u0000\u0000\u0000VW\u0001\u0000\u0000\u0000Wj\u0001\u0000\u0000\u0000"+
		"XZ\u0005\u0012\u0000\u0000YX\u0001\u0000\u0000\u0000Z[\u0001\u0000\u0000"+
		"\u0000[Y\u0001\u0000\u0000\u0000[\\\u0001\u0000\u0000\u0000\\]\u0001\u0000"+
		"\u0000\u0000]_\u0005\u000b\u0000\u0000^`\u0005\u0012\u0000\u0000_^\u0001"+
		"\u0000\u0000\u0000`a\u0001\u0000\u0000\u0000a_\u0001\u0000\u0000\u0000"+
		"ab\u0001\u0000\u0000\u0000bc\u0001\u0000\u0000\u0000ce\u0005\n\u0000\u0000"+
		"df\u0005\u0012\u0000\u0000ed\u0001\u0000\u0000\u0000fg\u0001\u0000\u0000"+
		"\u0000ge\u0001\u0000\u0000\u0000gh\u0001\u0000\u0000\u0000hj\u0001\u0000"+
		"\u0000\u0000iL\u0001\u0000\u0000\u0000iN\u0001\u0000\u0000\u0000iY\u0001"+
		"\u0000\u0000\u0000j\u000b\u0001\u0000\u0000\u0000km\u0005\u0012\u0000"+
		"\u0000lk\u0001\u0000\u0000\u0000mp\u0001\u0000\u0000\u0000nl\u0001\u0000"+
		"\u0000\u0000no\u0001\u0000\u0000\u0000oq\u0001\u0000\u0000\u0000pn\u0001"+
		"\u0000\u0000\u0000qr\u0007\u0001\u0000\u0000r\r\u0001\u0000\u0000\u0000"+
		"st\u0003\f\u0006\u0000tu\u0003\n\u0005\u0000uv\u0003\f\u0006\u0000v\u0088"+
		"\u0001\u0000\u0000\u0000wy\u0005\u0012\u0000\u0000xw\u0001\u0000\u0000"+
		"\u0000y|\u0001\u0000\u0000\u0000zx\u0001\u0000\u0000\u0000z{\u0001\u0000"+
		"\u0000\u0000{}\u0001\u0000\u0000\u0000|z\u0001\u0000\u0000\u0000}~\u0005"+
		"\b\u0000\u0000~\u0082\u0003\u0014\n\u0000\u007f\u0081\u0005\u0012\u0000"+
		"\u0000\u0080\u007f\u0001\u0000\u0000\u0000\u0081\u0084\u0001\u0000\u0000"+
		"\u0000\u0082\u0080\u0001\u0000\u0000\u0000\u0082\u0083\u0001\u0000\u0000"+
		"\u0000\u0083\u0085\u0001\u0000\u0000\u0000\u0084\u0082\u0001\u0000\u0000"+
		"\u0000\u0085\u0086\u0005\t\u0000\u0000\u0086\u0088\u0001\u0000\u0000\u0000"+
		"\u0087s\u0001\u0000\u0000\u0000\u0087z\u0001\u0000\u0000\u0000\u0088\u000f"+
		"\u0001\u0000\u0000\u0000\u0089\u008d\u0003\u000e\u0007\u0000\u008a\u008c"+
		"\u0005\u0012\u0000\u0000\u008b\u008a\u0001\u0000\u0000\u0000\u008c\u008f"+
		"\u0001\u0000\u0000\u0000\u008d\u008b\u0001\u0000\u0000\u0000\u008d\u008e"+
		"\u0001\u0000\u0000\u0000\u008e\u0090\u0001\u0000\u0000\u0000\u008f\u008d"+
		"\u0001\u0000\u0000\u0000\u0090\u0091\u0005\f\u0000\u0000\u0091\u0092\u0003"+
		"\u000e\u0007\u0000\u0092\u0095\u0001\u0000\u0000\u0000\u0093\u0095\u0003"+
		"\u000e\u0007\u0000\u0094\u0089\u0001\u0000\u0000\u0000\u0094\u0093\u0001"+
		"\u0000\u0000\u0000\u0095\u0011\u0001\u0000\u0000\u0000\u0096\u009a\u0003"+
		"\u0010\b\u0000\u0097\u0099\u0005\u0012\u0000\u0000\u0098\u0097\u0001\u0000"+
		"\u0000\u0000\u0099\u009c\u0001\u0000\u0000\u0000\u009a\u0098\u0001\u0000"+
		"\u0000\u0000\u009a\u009b\u0001\u0000\u0000\u0000\u009b\u009d\u0001\u0000"+
		"\u0000\u0000\u009c\u009a\u0001\u0000\u0000\u0000\u009d\u009e\u0005\r\u0000"+
		"\u0000\u009e\u009f\u0003\u0010\b\u0000\u009f\u00a2\u0001\u0000\u0000\u0000"+
		"\u00a0\u00a2\u0003\u0010\b\u0000\u00a1\u0096\u0001\u0000\u0000\u0000\u00a1"+
		"\u00a0\u0001\u0000\u0000\u0000\u00a2\u0013\u0001\u0000\u0000\u0000\u00a3"+
		"\u00a4\u0003\u0012\t\u0000\u00a4\u0015\u0001\u0000\u0000\u0000\u00a5\u00a9"+
		"\u0005\u000e\u0000\u0000\u00a6\u00a8\u0005\u0012\u0000\u0000\u00a7\u00a6"+
		"\u0001\u0000\u0000\u0000\u00a8\u00ab\u0001\u0000\u0000\u0000\u00a9\u00a7"+
		"\u0001\u0000\u0000\u0000\u00a9\u00aa\u0001\u0000\u0000\u0000\u00aa\u00ac"+
		"\u0001\u0000\u0000\u0000\u00ab\u00a9\u0001\u0000\u0000\u0000\u00ac\u00ad"+
		"\u0003\u0014\n\u0000\u00ad\u0017\u0001\u0000\u0000\u0000\u00ae\u00af\u0005"+
		"\u0018\u0000\u0000\u00af\u0019\u0001\u0000\u0000\u0000\u00b0\u00c1\u0003"+
		"\u0018\f\u0000\u00b1\u00b3\u0005\u0012\u0000\u0000\u00b2\u00b1\u0001\u0000"+
		"\u0000\u0000\u00b3\u00b6\u0001\u0000\u0000\u0000\u00b4\u00b2\u0001\u0000"+
		"\u0000\u0000\u00b4\u00b5\u0001\u0000\u0000\u0000\u00b5\u00b7\u0001\u0000"+
		"\u0000\u0000\u00b6\u00b4\u0001\u0000\u0000\u0000\u00b7\u00bb\u0005\u0007"+
		"\u0000\u0000\u00b8\u00ba\u0005\u0012\u0000\u0000\u00b9\u00b8\u0001\u0000"+
		"\u0000\u0000\u00ba\u00bd\u0001\u0000\u0000\u0000\u00bb\u00b9\u0001\u0000"+
		"\u0000\u0000\u00bb\u00bc\u0001\u0000\u0000\u0000\u00bc\u00be\u0001\u0000"+
		"\u0000\u0000\u00bd\u00bb\u0001\u0000\u0000\u0000\u00be\u00c0\u0003\u0018"+
		"\f\u0000\u00bf\u00b4\u0001\u0000\u0000\u0000\u00c0\u00c3\u0001\u0000\u0000"+
		"\u0000\u00c1\u00bf\u0001\u0000\u0000\u0000\u00c1\u00c2\u0001\u0000\u0000"+
		"\u0000\u00c2\u001b\u0001\u0000\u0000\u0000\u00c3\u00c1\u0001\u0000\u0000"+
		"\u0000\u00c4\u00c8\u0005\u000f\u0000\u0000\u00c5\u00c7\u0005\u0012\u0000"+
		"\u0000\u00c6\u00c5\u0001\u0000\u0000\u0000\u00c7\u00ca\u0001\u0000\u0000"+
		"\u0000\u00c8\u00c6\u0001\u0000\u0000\u0000\u00c8\u00c9\u0001\u0000\u0000"+
		"\u0000\u00c9\u00cc\u0001\u0000\u0000\u0000\u00ca\u00c8\u0001\u0000\u0000"+
		"\u0000\u00cb\u00cd\u0003\u001a\r\u0000\u00cc\u00cb\u0001\u0000\u0000\u0000"+
		"\u00cc\u00cd\u0001\u0000\u0000\u0000\u00cd\u00d1\u0001\u0000\u0000\u0000"+
		"\u00ce\u00d0\u0005\u0012\u0000\u0000\u00cf\u00ce\u0001\u0000\u0000\u0000"+
		"\u00d0\u00d3\u0001\u0000\u0000\u0000\u00d1\u00cf\u0001\u0000\u0000\u0000"+
		"\u00d1\u00d2\u0001\u0000\u0000\u0000\u00d2\u00d4\u0001\u0000\u0000\u0000"+
		"\u00d3\u00d1\u0001\u0000\u0000\u0000\u00d4\u00d5\u0005\u0010\u0000\u0000"+
		"\u00d5\u001d\u0001\u0000\u0000\u0000\u00d6\u00da\u0003\u0018\f\u0000\u00d7"+
		"\u00d9\u0005\u0012\u0000\u0000\u00d8\u00d7\u0001\u0000\u0000\u0000\u00d9"+
		"\u00dc\u0001\u0000\u0000\u0000\u00da\u00d8\u0001\u0000\u0000\u0000\u00da"+
		"\u00db\u0001\u0000\u0000\u0000\u00db\u00de\u0001\u0000\u0000\u0000\u00dc"+
		"\u00da\u0001\u0000\u0000\u0000\u00dd\u00df\u0003\u001c\u000e\u0000\u00de"+
		"\u00dd\u0001\u0000\u0000\u0000\u00de\u00df\u0001\u0000\u0000\u0000\u00df"+
		"\u00e3\u0001\u0000\u0000\u0000\u00e0\u00e2\u0005\u0012\u0000\u0000\u00e1"+
		"\u00e0\u0001\u0000\u0000\u0000\u00e2\u00e5\u0001\u0000\u0000\u0000\u00e3"+
		"\u00e1\u0001\u0000\u0000\u0000\u00e3\u00e4\u0001\u0000\u0000\u0000\u00e4"+
		"\u00e7\u0001\u0000\u0000\u0000\u00e5\u00e3\u0001\u0000\u0000\u0000\u00e6"+
		"\u00e8\u0003\b\u0004\u0000\u00e7\u00e6\u0001\u0000\u0000\u0000\u00e7\u00e8"+
		"\u0001\u0000\u0000\u0000\u00e8\u00ec\u0001\u0000\u0000\u0000\u00e9\u00eb"+
		"\u0005\u0012\u0000\u0000\u00ea\u00e9\u0001\u0000\u0000\u0000\u00eb\u00ee"+
		"\u0001\u0000\u0000\u0000\u00ec\u00ea\u0001\u0000\u0000\u0000\u00ec\u00ed"+
		"\u0001\u0000\u0000\u0000\u00ed\u00f0\u0001\u0000\u0000\u0000\u00ee\u00ec"+
		"\u0001\u0000\u0000\u0000\u00ef\u00f1\u0003\u0016\u000b\u0000\u00f0\u00ef"+
		"\u0001\u0000\u0000\u0000\u00f0\u00f1\u0001\u0000\u0000\u0000\u00f1\u001f"+
		"\u0001\u0000\u0000\u0000\u00f2\u00f4\u0005\u0012\u0000\u0000\u00f3\u00f2"+
		"\u0001\u0000\u0000\u0000\u00f4\u00f7\u0001\u0000\u0000\u0000\u00f5\u00f3"+
		"\u0001\u0000\u0000\u0000\u00f5\u00f6\u0001\u0000\u0000\u0000\u00f6\u00f8"+
		"\u0001\u0000\u0000\u0000\u00f7\u00f5\u0001\u0000\u0000\u0000\u00f8\u00fc"+
		"\u0003\u001e\u000f\u0000\u00f9\u00fb\u0005\u0012\u0000\u0000\u00fa\u00f9"+
		"\u0001\u0000\u0000\u0000\u00fb\u00fe\u0001\u0000\u0000\u0000\u00fc\u00fa"+
		"\u0001\u0000\u0000\u0000\u00fc\u00fd\u0001\u0000\u0000\u0000\u00fd\u0102"+
		"\u0001\u0000\u0000\u0000\u00fe\u00fc\u0001\u0000\u0000\u0000\u00ff\u0101"+
		"\u0005\u0011\u0000\u0000\u0100\u00ff\u0001\u0000\u0000\u0000\u0101\u0104"+
		"\u0001\u0000\u0000\u0000\u0102\u0100\u0001\u0000\u0000\u0000\u0102\u0103"+
		"\u0001\u0000\u0000\u0000\u0103!\u0001\u0000\u0000\u0000\u0104\u0102\u0001"+
		"\u0000\u0000\u0000$%+4;BJPV[aginz\u0082\u0087\u008d\u0094\u009a\u00a1"+
		"\u00a9\u00b4\u00bb\u00c1\u00c8\u00cc\u00d1\u00da\u00de\u00e3\u00e7\u00ec"+
		"\u00f0\u00f5\u00fc\u0102";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
