package ru.tgmaksim.gymnasium.fragment

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import androidx.fragment.app.Fragment
import android.annotation.SuppressLint
import androidx.activity.OnBackPressedCallback

import ru.tgmaksim.gymnasium.api.Constants
import ru.tgmaksim.gymnasium.databinding.FragmentWebViewBinding

class WebViewFragment : Fragment() {
    private lateinit var ui: FragmentWebViewBinding
    private var url: String? = null

    companion object {
        private const val ARG_URL = "arg_url"

        fun newInstance(url: String): WebViewFragment {
            val fragment = WebViewFragment()

            // Открытие документов через google
            fragment.arguments = Bundle().apply {
                putString(ARG_URL, Constants.DOCS_VIEW_ULR + url)
            }

            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        url = arguments?.getString(ARG_URL)

        // Перехват кнопки "Назад" — если WebView может вернуться назад, он возвращается
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (ui.webView.canGoBack()) {
                        ui.webView.goBack()
                    } else {
                        // Возвращение назад и перерисовка
                        parentFragmentManager.popBackStack()
                        requireActivity().recreate()
                    }
                }
            })
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = FragmentWebViewBinding.inflate(inflater, container, false)

        ui.webLoading.visibility = View.VISIBLE

        ui.webView.settings.apply {
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
            loadWithOverviewMode = true
            javaScriptEnabled = true
            domStorageEnabled = true
        }

        ui.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Если страница так и не загрузилась
                if (view?.contentHeight == 0)
                    view.reload()
                else
                    ui.webLoading.visibility = View.GONE
            }
        }
        ui.webView.webChromeClient = WebChromeClient()

        url?.let { ui.webView.loadUrl(it) }

        return ui.root
    }
}