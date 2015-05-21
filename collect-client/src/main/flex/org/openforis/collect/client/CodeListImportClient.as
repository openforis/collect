package org.openforis.collect.client {
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	
	import org.openforis.collect.io.parsing.CSVFileOptions;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class CodeListImportClient extends AbstractImportProcessClient {
		
		public function CodeListImportClient() {
			super("codeListImportService");
		}
		
		public function start(responder:IResponder, codeListId:int, tempFileName:String, csvFileOptions:CSVFileOptions, overwriteAllData:Boolean = true):void {
			var token:AsyncToken = this._startOperation.send(codeListId, tempFileName, csvFileOptions, overwriteAllData);
			token.addResponder(responder);
		}

	}
}